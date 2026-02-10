package sisosolsol.greenfire.image.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImageUploadDTO {
    private Integer imageCode;
    private String path;         // 상대 경로: "notice/20240108/uuid_filename.jpg"
    private String originName;   // 원본 파일명
    private String fileName;     // 저장된 파일명 (UUID_원본파일명)
}