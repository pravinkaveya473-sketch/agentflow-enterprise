package com.cjl.java_ai_agent.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, String> {
    Optional<AppUserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
