package sisosolsol.greenfire.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sisosolsol.greenfire.common.security.handler.JwtAccessDeniedHandler;
import sisosolsol.greenfire.common.security.handler.JwtAuthenticationEntryPoint;
import sisosolsol.greenfire.common.security.filter.JwtAuthenticationFilter;
import sisosolsol.greenfire.common.security.jwt.JwtUtil;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.
                // 불필요한 인증 방식 비활성화
                formLogin(AbstractHttpConfigurer::disable)  // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증 비활성화 (보안상 안전)

                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 비활성화 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // JWT 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/api/public/**").permitAll();
                        auth.requestMatchers("/api/v1/challenge/**").permitAll();
                        auth.requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN");
                        auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
                        auth.requestMatchers("/api/manager/**").hasRole("MANAGER");
                        // 그 외 모든 리소스 (인증 필요)
                        // auth.anyRequest().authenticated();
                        auth.anyRequest().permitAll();
                })
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.accessDeniedHandler(jwtAccessDeniedHandler());
                    exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint());
                })
                .build();
    }

    @Bean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }
}