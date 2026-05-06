package sisosolsol.greenfire.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sisosolsol.greenfire.common.security.model.UserRole;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 카카오/네이버 등 OAuth provider 응답을 받아 우리 users + auth_account에 upsert.
 * 키 식별: 응답에서 받은 email (카카오는 동의 항목 'account_email' 필요).
 *
 * 신규 사용자: nickname/name = provider nickname, password = 사용 불가능한 BCrypt 해시.
 * 기존 사용자: email 일치하면 그 계정으로 로그인 (provider 충돌은 데모용 단순 처리).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    public static final String ATTR_USER_CODE = "_userCode";
    public static final String ATTR_EMAIL     = "_email";
    public static final String ATTR_ROLE      = "_role";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserAccountRepository userAccountRepository;
    // PasswordEncoder를 빈 주입하면 SecurityConfig와 순환. unusable hash만 만들면 충분해 직접 인스턴스화.
    private static final PasswordEncoder PW_ENCODER = new BCryptPasswordEncoder();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User raw = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthProfile profile = extractProfile(registrationId, raw.getAttributes());
        if (profile == null) {
            throw new OAuth2AuthenticationException("OAuth 응답 파싱 실패");
        }
        if (profile.email() == null || profile.email().isBlank()) {
            throw new OAuth2AuthenticationException("이메일 동의가 필요합니다");
        }

        UserAccount user = userAccountRepository.findByEmail(profile.email())
                .orElseGet(() -> createNewUser(profile));

        // principal attributes에 우리 user 정보 + 카카오 user-name-attribute(id) 모두 포함
        Map<String, Object> attributes = new HashMap<>(raw.getAttributes());
        attributes.put(ATTR_USER_CODE, user.getId().toString());
        attributes.put(ATTR_EMAIL, user.getEmail());
        attributes.put(ATTR_ROLE, user.getRole().name());

        String nameAttribute = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                nameAttribute
        );
    }

    private UserAccount createNewUser(OAuthProfile profile) {
        // 사용자 입력 비밀번호로는 절대 매칭 안 되도록 random 해시
        String unusablePassword = PW_ENCODER.encode("oauth_" + UUID.randomUUID());
        String nickname = profile.nickname() != null ? profile.nickname() : "user_" + UUID.randomUUID().toString().substring(0, 6);
        UserAccount fresh = new UserAccount(
                profile.email(),
                unusablePassword,
                UserRole.USER,
                nickname,
                nickname,    // name = nickname (사용자가 마이페이지에서 수정)
                null, null, null
        );
        UserAccount saved = userAccountRepository.save(fresh);
        log.info("OAuth 신규 가입: provider={}, email={}, userId={}", profile.provider(), profile.email(), saved.getId());
        return saved;
    }

    /** 카카오 응답 → OAuthProfile. 다른 provider 추가 시 분기 추가. */
    @SuppressWarnings("unchecked")
    private OAuthProfile extractProfile(String registrationId, Map<String, Object> attrs) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> account = (Map<String, Object>) attrs.get("kakao_account");
            if (account == null) return null;
            String email = (String) account.get("email");
            String nickname = null;
            Map<String, Object> kakaoProfile = (Map<String, Object>) account.get("profile");
            if (kakaoProfile != null) nickname = (String) kakaoProfile.get("nickname");
            return new OAuthProfile("kakao", email, nickname);
        }
        // 추후 naver / google: 여기 분기 추가
        return null;
    }

    private record OAuthProfile(String provider, String email, String nickname) {}
}
