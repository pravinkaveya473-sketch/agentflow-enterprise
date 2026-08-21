package com.cjl.java_ai_agent.security;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AppUserRepository users;

    public AdminUserController(AppUserRepository users) {
        this.users = users;
    }

    @PatchMapping("/{username}/role")
    @Transactional
    public ResponseEntity<ApiResponse<UserAdminView>> changeRole(
            @PathVariable String username,
            @Valid @RequestBody ChangeRoleRequest request) {
        AppUserEntity user = require(username);
        user.changeRole(request.role());
        return ResponseEntity.ok(ApiResponse.success(UserAdminView.from(user)));
    }

    @PatchMapping("/{username}/enabled")
    @Transactional
    public ResponseEntity<ApiResponse<UserAdminView>> changeEnabled(
            @PathVariable String username,
            @Valid @RequestBody ChangeEnabledRequest request) {
        AppUserEntity user = require(username);
        user.setEnabled(request.enabled());
        return ResponseEntity.ok(ApiResponse.success(UserAdminView.from(user)));
    }

    private AppUserEntity require(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "用户不存在：" + username));
    }

    public record ChangeRoleRequest(@NotNull Role role) {}
    public record ChangeEnabledRequest(@NotNull Boolean enabled) {}
    public record UserAdminView(String username, Role role, boolean enabled) {
        static UserAdminView from(AppUserEntity user) {
            return new UserAdminView(user.getUsername(), user.getRole(), user.isEnabled());
        }
    }
}
