package sisosolsol.greenfire.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private String query;
    private String type;            // 요청 type 그대로 echo
    private List<SearchHit> items;
    private int total;
}
