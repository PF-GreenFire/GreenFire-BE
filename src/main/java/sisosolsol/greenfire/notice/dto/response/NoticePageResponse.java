package sisosolsol.greenfire.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NoticePageResponse {

    private List<NoticeListResponse> notices;
    private Integer total;
    private Integer page;
    private Integer limit;
    private Boolean hasMore;

    public static NoticePageResponse from(Page<NoticeListResponse> page) {
        return NoticePageResponse.builder()
                .notices(page.getContent())
                .total((int) page.getTotalElements())
                .page(page.getNumber() + 1)  // 0-based to 1-based
                .limit(page.getSize())
                .hasMore(page.hasNext())
                .build();
    }
}