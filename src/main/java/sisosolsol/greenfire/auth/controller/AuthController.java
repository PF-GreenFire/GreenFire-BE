package sisosolsol.greenfire.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.auth.dto.*;
import sisosolsol.greenfire.auth.service.AuthService;
import sisosolsol.greenfire.common.security.jwt.JwtUtil;
import sisosolsol.greenfire.common.security.model.AuthUser;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/auth";

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final sisosolsol.greenfire.spark.service.SparkService sparkService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> signup(@Valid @RequestPart("data") SignupRequest req,
                                       @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                                       HttpServletRequest request) {
        authService.signup(req, profileImage, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(req, request.getRemoteAddr());
        addRefreshCookie(response, result.rawRefreshToken());
        return ResponseEntity.ok(result.tokenResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request,
                                                 HttpServletResponse response) {
        String rawRefreshToken = extractRefreshToken(request);
        if (rawRefreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        AuthService.RefreshResult result = authService.refresh(rawRefreshToken, request.getRemoteAddr());
        addRefreshCookie(response, result.rawRefreshToken());
        return ResponseEntity.ok(result.tokenResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {
        String rawRefreshToken = extractRefreshToken(request);
        if (rawRefreshToken != null) {
            authService.logout(rawRefreshToken, request.getRemoteAddr());
        }
        deleteRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        int total = sparkService.getTotalSpark(user.userId());
        return new MeResponse(
                true, user.userId(), user.email(), user.role(),
                sisosolsol.greenfire.spark.model.dto.SparkInfo.from(total)
        );
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @PostMapping("/find-email")
    public ResponseEntity<FindEmailResponse> findEmail(@Valid @RequestBody FindEmailRequest req) {
        FindEmailResponse result = authService.findEmail(req.email());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password-reset/send-code")
    public ResponseEntity<Map<String, String>> sendResetCode(@Valid @RequestBody SendResetCodeRequest req) {
        authService.sendResetCode(req.email());
        return ResponseEntity.ok(Map.of("message", "인증 코드가 발송되었습니다."));
    }

    @PostMapping("/password-reset/verify-code")
    public ResponseEntity<Map<String, Boolean>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest req) {
        authService.verifyResetCode(req.email(), req.code());
        return ResponseEntity.ok(Map.of("verified", true));
    }

    @PostMapping("/password-reset/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.email(), req.code(), req.newPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequest req,
                                              Authentication authentication,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        authService.deleteAccount(user.userId(), req.password(), req.reason(), request.getRemoteAddr());
        deleteRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    // ── Cookie helpers ──────────────────────────────────────────

    private void addRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, rawRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) jwtUtil.getRefreshExpSeconds());
        cookie.setAttribute("SameSite", cookieSecure ? "Strict" : "Lax");
        response.addCookie(cookie);
    }

    private void deleteRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", cookieSecure ? "Strict" : "Lax");
        response.addCookie(cookie);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
