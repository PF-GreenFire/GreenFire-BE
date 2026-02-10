package sisosolsol.greenfire.common.audit.entity;

public enum ActionType {

    // 인증
    LOGIN,
    LOGOUT,
    SIGNUP,
    REFRESH,
    DELETE_ACCOUNT,

    // CRUD
    CREATE,
    UPDATE,
    DELETE,

    // 열람/인게이지먼트
    VIEW,
    LIKE,
    UNLIKE,
    BOOKMARK,
    UNBOOKMARK,
    SHARE,

    // 소셜
    FOLLOW,
    UNFOLLOW,

    // 챌린지 참여
    JOIN,
    LEAVE,
    COMPLETE,

    // 신고
    REPORT,

    // 관리자 조치
    SUSPEND,
    UNSUSPEND
}
