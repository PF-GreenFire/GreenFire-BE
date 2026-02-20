package sisosolsol.greenfire.common.enums.image;

import lombok.Getter;

@Getter
public enum ImageType {
    STORE("장소"),
    POST("게시글"),
    NOTICE("공지사항"),
    CHALLENGE("챌린지"),
    PROFILE("프로필");

    private final String description;

    ImageType(String description) {
        this.description = description;
    }
}