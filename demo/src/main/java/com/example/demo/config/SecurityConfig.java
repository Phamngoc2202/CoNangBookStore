
package com.example.demo.config;

import java.util.Collections;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // 1. Mã hóa mật khẩu bằng BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Cấu hình quyền truy cập API
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            // API REST sử dụng JWT, không sử dụng session đăng nhập
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Cho phép preflight request từ trình duyệt
                .requestMatchers(
                    HttpMethod.OPTIONS, "/**"
                ).permitAll()

                // Đăng ký và đăng nhập không cần JWT
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // API xem sách công khai
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/books",
                    "/api/books/**"
                ).permitAll()

                // API xem danh mục công khai
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/categories",
                    "/api/categories/**"
                ).permitAll()

                // Quản trị hệ thống: chỉ ADMIN
                .requestMatchers(
                    "/api/admin/**",
                    "/api/reports/**"
                ).hasAuthority("ADMIN")

                // Quản lý sách
                .requestMatchers(
                    HttpMethod.POST, "/api/books"
                ).hasAnyAuthority("STAFF", "ADMIN")

                .requestMatchers(
                    HttpMethod.PUT, "/api/books/**"
                ).hasAnyAuthority("STAFF", "ADMIN")

                .requestMatchers(
                    HttpMethod.PATCH, "/api/books/**"
                ).hasAnyAuthority("STAFF", "ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/books/**"
                ).hasAuthority("ADMIN")

                // Tạo đơn hàng: người đã đăng nhập
                .requestMatchers(
                    HttpMethod.POST, "/api/orders"
                ).authenticated()

                // Phê duyệt, từ chối, giao hàng, hoàn thành:
                // chỉ STAFF hoặc ADMIN
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/orders/*/approve",
                    "/api/orders/*/reject",
                    "/api/orders/*/ship",
                    "/api/orders/*/complete"
                ).hasAnyAuthority("STAFF", "ADMIN")

                // Hủy đơn: yêu cầu đăng nhập.
                // Chủ đơn phải được kiểm tra thêm ở Service.
                .requestMatchers(
                    HttpMethod.POST, "/api/orders/*/cancel"
                ).authenticated()

                // Xem đơn hàng và lịch sử: yêu cầu đăng nhập.
                // Service phải kiểm tra quyền sở hữu đơn.
                .requestMatchers(
                    HttpMethod.GET, "/api/orders/**"
                ).authenticated()

                // Các API còn lại yêu cầu xác thực
                .anyRequest().authenticated()
            )

            // Xác thực JWT theo chuẩn OAuth2 Resource Server
            // .oauth2ResourceServer(oauth2 ->
            //     oauth2.jwt(Customizer.withDefaults())
            // );
            // Xác thực JWT theo chuẩn OAuth2 Resource Server với bộ chuyển đổi quyền tùy chỉnh
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }
// 4. Bean chuyển đổi trường "role" từ JWT thành GrantedAuthority cho Spring Security
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            // Đọc trường "role" từ token JWT (khớp với key "role" bạn tạo lúc đăng nhập)
            String role = jwt.getClaimAsString("role");
            
            Collection<GrantedAuthority> authorities;
            if (role != null && !role.isEmpty()) {
                // Spring Security yêu cầu quyền phải có dạng "ROLE_" nếu bạn dùng hasRole, 
                // hoặc giữ nguyên nếu dùng hasAuthority. 
                // Vì bạn đang dùng hasAuthority("ADMIN"), ta truyền thẳng chữ "ADMIN" hoặc "admin" (nên viết hoa).
                authorities = Collections.singletonList(new SimpleGrantedAuthority(role.toUpperCase()));
            } else {
                authorities = Collections.emptyList();
            }

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
    // 3. Cấu hình CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Cho phép mọi origin; phù hợp khi phát triển.
        config.setAllowedOriginPatterns(List.of("*"));

        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        // Cho phép trình duyệt đọc response header Authorization
        config.setExposedHeaders(List.of("Authorization"));

        // Không sử dụng cookie xác thực cross-origin
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
