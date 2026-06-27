package sisosolsol.greenfire.common.exception.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    /*
     * 401: Unauthorized
     * 403: Forbidden
     * 404: Not Found
     * 405: Method Not Allowed
     * 409: Conflict
     * 500: Server Error
     */

    // 401 Error
    UNAUTHORIZED(401, "인증 되지 않은 요청입니다."),

    // 403 Error
    ACCESS_DENIED(403, "허가 되지 않은 요청입니다."),
    ACCOUNT_DELETED(403, "탈퇴한 계정입니다."),
    ACCOUNT_SUSPENDED(403, "정지된 계정입니다."),

    // 404 Error
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    CHALLENGE_NOT_FOUND(404, "존재하지 않는 챌린지입니다."),
    NOTICE_NOT_FOUND(404, "공지사항을 찾을 수 없습니다."),

    // 409 Error
    EMAIL_ALREADY_EXISTS(409, "이미 가입된 이메일입니다."),
    REJOIN_COOLDOWN(409, "탈퇴 후 30일간 동일 이메일로 재가입할 수 없습니다."),
    CHALLENGE_INVALID_STATUS(409, "모집 중인 챌린지만 가능합니다."),
    CHALLENGE_ALREADY_STARTED(409, "이미 시작된 챌린지입니다."),
    CHALLENGE_ALREADY_PARTICIPATED(409, "이미 참여 중인 챌린지입니다."),
    CHALLENGE_FULL_CAPACITY(409, "모집 정원이 마감되었습니다."),
    CHALLENGE_NOT_PARTICIPATED(409, "참여하지 않은 챌린지입니다."),
    CHALLENGE_ALREADY_COMPLETED(409, "이미 종료된 챌린지입니다."),
    CHALLENGE_ALREADY_CANCELLED(409, "이미 취소된 챌린지입니다."),
    CHALLENGE_PAUSED(409, "일시중지된 챌린지입니다."),
    CHALLENGE_CANCEL_FAILED(409, "챌린지 취소에 실패했습니다."),
    CATEGORY_IN_USE(409, "사용 중인 카테고리는 삭제할 수 없습니다."),
    INVALID_STORE_STATUS(400, "유효하지 않은 장소 상태값입니다."),


    // 400 BAD REQUEST
    WEAK_PASSWORD(400, "비밀번호는 8자 이상, 대문자·소문자·숫자·특수문자를 각 1개 이상 포함해야 합니다."),
    PASSWORD_MISMATCH(400, "현재 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD(400, "현재 비밀번호와 다른 비밀번호를 입력해주세요."),
    RESET_CODE_EXPIRED(400, "인증 코드가 만료되었습니다."),
    RESET_CODE_INVALID(400, "유효하지 않은 인증 코드입니다."),
    RESET_CODE_NOT_VERIFIED(400, "이메일 인증이 완료되지 않았습니다."),
    POST_TYPE_MISMATCH(400, "적합하지 않은 게시물 타입입니다."),
    FILE_SIZE_EXCEEDED(400, "파일 크기가 제한을 초과했습니다."),
    FILE_TYPE_NOT_ALLOWED(400, "허용되지 않은 파일 형식입니다."),
    CANNOT_CHANGE_OWN_ROLE(400, "본인의 역할은 변경할 수 없습니다."),
    INVALID_ROLE(400, "유효하지 않은 역할입니다."),
    CANNOT_SUSPEND_ADMIN(400, "관리자 계정은 정지할 수 없습니다."),
    CANNOT_SUSPEND_SELF(400, "본인 계정은 정지할 수 없습니다."),
    FILE_NOT_FOUND(404, "파일을 찾을 수 없습니다."),

    InvalidForeignKeyException(1100, "외래 키 제약을 위반한 요청입니다."),
    INVALID_FOREIGN_KEY(409, "외래 키 제약을 위반한 요청입니다."),
    DUPLICATE_REPORT(409, "이미 신고한 콘텐츠입니다."),
    REPORT_ALREADY_HANDLED(409, "이미 처리된 신고입니다."),

    // 404 Error - Report
    REPORT_NOT_FOUND(404, "신고를 찾을 수 없습니다."),

    // 400 Error - Report
    INVALID_RESOURCE_TYPE(400, "유효하지 않은 리소스 타입입니다."),
    INVALID_REPORT_CATEGORY(400, "유효하지 않은 신고 카테고리입니다."),
    INVALID_REPORT_STATUS(400, "유효하지 않은 신고 상태입니다."),

    // 500 Error
    DATABASE_ACCESS_ERROR(500, "데이터베이스 접근 중 오류가 발생했습니다."),
    FILE_UPLOAD_ERROR(500, "파일 업로드 중 오류가 발생했습니다."),
    EMAIL_SEND_FAILED(500, "이메일 발송에 실패했습니다.");


    private final int code;
    private final String message;

}