package sisosolsol.greenfire.scrap.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sisosolsol.greenfire.scrap.model.ScrapTargetType;

@Getter
@Setter
public class ScrapCreateDTO {
    @NotNull
    private ScrapTargetType targetType;

    @NotBlank
    private String targetCode;
}
