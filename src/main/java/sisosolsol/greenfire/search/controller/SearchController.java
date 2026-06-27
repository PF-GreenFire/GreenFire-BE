package sisosolsol.greenfire.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.search.dto.SearchResponse;
import sisosolsol.greenfire.search.model.SearchType;
import sisosolsol.greenfire.search.service.SearchService;

@Tag(name = "통합 검색", description = "도메인별 통합 검색 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 통합 검색 진입점. type 별로 호출하면 그 도메인 결과만, ALL이면 모든 도메인 결과를 한 배열로.
     *
     * 예: GET /api/search?q=초록&type=POST&size=20
     */
    @Operation(summary = "통합 검색 (type별 또는 전체 도메인)")
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam("q") String q,
            @RequestParam(value = "type", defaultValue = "ALL") SearchType type,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (size < 1 || size > 100) size = 20;

        return ResponseEntity.ok(searchService.search(q.trim(), type, size));
    }
}
