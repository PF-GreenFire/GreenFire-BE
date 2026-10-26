package sisosolsol.greenfire.challenge.model.dto.completion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** 참여자별 인증한 날짜 (한국 시간 기준, 하루 1행) */
@Getter
@Setter
@NoArgsConstructor
public class CertificationDayRow {
    private UUID userCode;
    private LocalDate certDate;
}
