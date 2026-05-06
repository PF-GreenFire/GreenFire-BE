package sisosolsol.greenfire.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder를 SecurityConfig에서 빼서 별 @Configuration에 둠.
 * SecurityConfig가 OAuth2 핸들러(→ AuthService → PasswordEncoder)를 의존하면
 * SecurityConfig 자체에서 빈 정의 시 순환이 생겨 부팅이 깨졌었음.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
