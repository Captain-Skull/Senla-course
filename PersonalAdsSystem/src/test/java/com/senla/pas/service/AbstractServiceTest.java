package com.senla.pas.service;

import com.senla.pas.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public abstract class AbstractServiceTest {

    @AfterEach
    protected void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected void authenticate(Long userId, String role) {
        CustomUserDetails details = new CustomUserDetails(userId, "u", "p", List.of(() -> role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, "p", details.getAuthorities())
        );
    }
}
