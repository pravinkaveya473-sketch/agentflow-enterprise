package com.cjl.java_ai_agent.security;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AppUserRepository users, PasswordEncoder encoder,
                          AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtService.TokenView>> register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new BusinessException("DATA_CONFLICT", "用户名已存在");
        }
        AppUserEntity user = users.save(new AppUserEntity(UUID.randomUUID().toString(), request.username(),
                encoder.encode(request.password()), Role.USER));
        return ResponseEntity.ok(ApiResponse.success(jwtService.issue(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtService.TokenView>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AppUserEntity user = users.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "用户不存在"));
        return ResponseEntity.ok(ApiResponse.success(jwtService.issue(user)));
    }

    public record RegisterRequest(
            @NotBlank
            @Size(min = 3, max = 64)
            @Pattern(regexp = "[A-Za-z0-9_.-]+", message = "用户名只能包含字母、数字、下划线、点和连字符")
            String username,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
}
