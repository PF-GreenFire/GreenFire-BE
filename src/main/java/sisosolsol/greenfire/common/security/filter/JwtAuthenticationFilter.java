package sisosolsol.greenfire.common.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import sisosolsol.greenfire.common.security.jwt.JwtUtil;
import sisosolsol.greenfire.common.security.model.AuthUser;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtUtil.extractClaims(token);

                AuthUser authUser = new AuthUser(
                        jwtUtil.getUserId(claims),
                        jwtUtil.getEmail(claims),
                        jwtUtil.getRole(claims)
                );

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + authUser.role()));

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (BadCredentialsException e) {
                // 토큰이 잘못돼도 public 엔드포인트는 통과시키기 위해
                // 여기서 응답을 확정하지 않고, 인증정보만 비우고 계속 진행.
                // protected 엔드포인트는 EntryPoint가 일관된 401(JSON)로 응답함.
                log.warn("Invalid JWT token (ignored): {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // ✅ 토큰 없이도 접근 가능한 엔드포인트만 제외
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/signup")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/logout")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/");
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
