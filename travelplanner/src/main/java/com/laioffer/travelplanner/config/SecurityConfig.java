package com.laioffer.travelplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 开发期先关 CSRF（若开启需前端携带 token）
                .csrf(csrf -> csrf.disable())
                // 允许跨域
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 预检请求放行（浏览器 CORS）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ===== 公开接口（不需要登录）=====
                        // 不带 /api 前缀的情况：
                        .requestMatchers(HttpMethod.GET, "/cities", "/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pois", "/pois/**").permitAll()
                        // 带 /api 前缀的情况（如果你的 Controller 是 /api/...）：
                        .requestMatchers(HttpMethod.GET, "/api/cities", "/api/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pois", "/api/pois/**").permitAll()

                        // 登录/注册放行
                        .requestMatchers("/auth/**").permitAll()

                        // 其它接口都需要登录（Session）
                        .anyRequest().authenticated()
                )
                // 我们使用自定义 JSON 登录，不用表单/Basic
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                // 注：退出登录接口 /auth/logout 已在 AuthController 中
                .logout(l -> l.logoutUrl("/auth/logout"));

        return http.build();
    }

    /** 允许前端 http://localhost:3000 跨域并携带 Cookie（JSESSIONID） */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 前端地址（开发环境）
        cfg.setAllowedOrigins(List.of("http://localhost:3000"));
        // 允许带 Cookie
        cfg.setAllowCredentials(true);
        // 允许的方法与头
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        // 可选：暴露某些响应头
        // cfg.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    /** BCrypt 密码编码器 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
