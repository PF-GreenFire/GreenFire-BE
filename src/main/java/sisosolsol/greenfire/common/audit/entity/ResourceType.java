package sisosolsol.greenfire.common.audit.entity;

public enum ResourceType {

    AUTH,           // 인증 이벤트 (로그인, 로그아웃, 토큰 갱신)
    ACCOUNT,        // 계정 관리 (가입, 탈퇴)
    NOTICE,         // 공지사항
    POST,           // 피드 게시글
    COMMENT,        // 댓글
    CHALLENGE,      // 챌린지
    USER,           // 유저 (팔로우/언팔로우 대상)
    REPORT          // 신고
}
