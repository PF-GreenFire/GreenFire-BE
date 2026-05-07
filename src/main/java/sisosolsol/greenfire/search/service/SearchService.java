package sisosolsol.greenfire.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sisosolsol.greenfire.search.dao.SearchMapper;
import sisosolsol.greenfire.search.dto.SearchHit;
import sisosolsol.greenfire.search.dto.SearchResponse;
import sisosolsol.greenfire.search.model.SearchType;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchMapper searchMapper;

    public SearchResponse search(String q, SearchType type, int size) {
        List<SearchHit> items = new ArrayList<>();

        if (type == SearchType.ALL || type == SearchType.CHALLENGE) {
            items.addAll(searchMapper.searchChallenges(q, size));
        }
        if (type == SearchType.ALL || type == SearchType.STORE) {
            items.addAll(searchMapper.searchStores(q, size));
        }
        if (type == SearchType.ALL || type == SearchType.POST) {
            items.addAll(searchMapper.searchPosts(q, size));
        }
        if (type == SearchType.ALL || type == SearchType.USER) {
            items.addAll(searchMapper.searchUsers(q, size));
        }

        return SearchResponse.builder()
                .query(q)
                .type(type.name())
                .items(items)
                .total(items.size())
                .build();
    }
}
