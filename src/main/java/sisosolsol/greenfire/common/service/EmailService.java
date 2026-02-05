package sisosolsol.greenfire.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[GreenFire] 비밀번호 재설정 인증 코드");
            helper.setText(buildHtmlContent(code), true);

            mailSender.send(message);
            log.info("비밀번호 재설정 인증 코드 발송 완료: {}", toEmail);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", toEmail, e);
            throw new CustomException(ExceptionCode.EMAIL_SEND_FAILED);
        }
    }

    private String buildHtmlContent(String code) {
        return """
                <div style="max-width:400px;margin:0 auto;padding:32px;font-family:'Apple SD Gothic Neo',sans-serif;">
                    <h2 style="color:#198754;margin-bottom:24px;">GreenFire 비밀번호 재설정</h2>
                    <p style="font-size:15px;color:#333;">아래 인증 코드를 입력해주세요.</p>
                    <div style="background:#f8f9fa;border:1px solid #dee2e6;border-radius:8px;padding:20px;text-align:center;margin:24px 0;">
                        <span style="font-size:32px;font-weight:700;letter-spacing:8px;color:#198754;">%s</span>
                    </div>
                    <p style="font-size:13px;color:#6c757d;">이 코드는 5분 동안 유효합니다.</p>
                    <p style="font-size:13px;color:#6c757d;">본인이 요청하지 않은 경우 이 이메일을 무시해주세요.</p>
                </div>
                """.formatted(code);
    }
}
