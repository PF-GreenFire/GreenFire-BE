package sisosolsol.greenfire.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ReportPageResponse {

    private List<ReportResponse> reports;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasMore;

    public static ReportPageResponse from(Page<ReportResponse> page) {
        return ReportPageResponse.builder()
                .reports(page.getContent())
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasMore(page.hasNext())
                .build();
    }
}
