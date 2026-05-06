package sisosolsol.greenfire.auth.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sisosolsol.greenfire.auth.service.AuthService;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.io.IOException;
import java.util.UUID;

/**
 * OAuth 인증 성공 후 우리 JWT(access+refresh) 발급 → refresh cookie 박고 FE로 redirect.
 *
 * FE는 useAuth가 마운트 시 fetchMe → access 없으면 refresh로 복구하는 흐름이 이미 있어서
 * 별도 callback 페이지 없이 메인(/) redirect만으로 자동 로그인이 완성된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/auth";

    private final AuthService authService;
    private final UserAccountRepository userAccountRepository;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String userCodeStr = (String) principal.getAttribute(CustomOAuth2UserService.ATTR_USER_CODE);

        if (userCodeStr == null) {
            response.sendRedirect(frontendUrl + "/?oauth=fail");
            return;
        }

        UserAccount user = userAccountRepository.findById(UUID.fromString(userCodeStr))
                .orElse(null);
        if (user == null) {
            response.sendRedirect(frontendUrl + "/?oauth=fail");
            return;
        }

        AuthService.LoginResult result = authService.issueTokensForOAuthUser(user, request.getRemoteAddr());
        addRefreshCookie(response, result.rawRefreshToken());
        // access token은 쿠키로 보내지 않음. FE useAuth가 mount 시 /api/auth/refresh 호출해서 받음.
        response.sendRedirect(frontendUrl + "/?oauth=success");
    }

    private void addRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, rawRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        // SameSite=Lax는 ResponseCookie를 써야 깔끔하지만 다른 곳과 동일 패턴 유지
        response.addCookie(cookie);
    }
}
