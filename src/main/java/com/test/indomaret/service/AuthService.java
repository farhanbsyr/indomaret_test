package com.test.indomaret.service;

import com.test.indomaret.dto.request.LoginRequest;
import com.test.indomaret.dto.request.RegisterRequest;
import com.test.indomaret.dto.response.AuthResponse;
import com.test.indomaret.dto.response.UserResponse;
import com.test.indomaret.entity.Role;
import com.test.indomaret.entity.User;
import com.test.indomaret.exception.BadRequestException;
import com.test.indomaret.exception.ResourceNotFoundException;
import com.test.indomaret.repository.UserRepository;
import com.test.indomaret.security.JwtTokenProvider;
import com.test.indomaret.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        auditLogService.recordChange("User", user.getId(), "LOGIN", null, "User logged in: " + user.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .expiresInMs(tokenProvider.getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_USER;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .build();

        user.setIsActive(true);
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);

        auditLogService.recordChange("User", savedUser.getId(), "REGISTER", null,
                "New user registered: " + savedUser.getUsername() + " (" + savedUser.getRole() + ")");

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new BadRequestException("No authenticated user found in context");
        }

        User user = userRepository.findByUsernameAndIsActiveTrueAndIsDeletedFalse(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", auth.getName()));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
