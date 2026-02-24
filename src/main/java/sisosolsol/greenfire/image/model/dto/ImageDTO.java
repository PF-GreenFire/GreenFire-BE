package sisosolsol.greenfire.image.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ImageDTO {
    private Integer imageCode;
    private String referenceType;  // POST, STORE, NOTICE, CHALLENGE
    private Integer referenceCode;
    private String path;
    private String originName;
    private String fileName;
    private Integer order;
}