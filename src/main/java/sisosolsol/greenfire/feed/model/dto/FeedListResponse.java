package sisosolsol.greenfire.feed.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeedListResponse {
    private List<FeedListItemDTO> content;
    private Integer nextCursorPostCode;
    private Double nextCursorScore;
    private boolean hasMore;
}
