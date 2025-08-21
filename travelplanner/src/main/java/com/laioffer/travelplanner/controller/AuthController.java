package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.entity.UserEntity;
import com.laioffer.travelplanner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * JSON 风格的注册 / 登录 / 会话查询
 * 如有全局前缀（/api），把 @RequestMapping("/auth") 改为 "/api/auth"
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository; // 从配置注入
    private final UserRepository userRepository;                        // 你的仓库
    private final PasswordEncoder passwordEncoder;                      // 与 SecurityConfig 一致

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册：POST /auth/register   Body: {"username","password"}
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String rawPassword = body.get("password");

        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username/password required"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "username exists"));
        }

        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(u);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "registered"));
    }

    /**
     * 登录（JSON）：POST /auth/login   Body: {"username","password"}
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 1) 当前请求上下文设置认证
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 2) ✅ 持久化到 Session（关键：让后续请求通过 JSESSIONID 还原认证）
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(Map.of("message", "login ok"));
    }

    /**
     * 查询登录态：GET /auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null ||
                authentication.getPrincipal() == null ||
                "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }
        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }
}
