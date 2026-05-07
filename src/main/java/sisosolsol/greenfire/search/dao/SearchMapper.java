package sisosolsol.greenfire.search.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.search.dto.SearchHit;

import java.util.List;

/**
 * 도메인별 검색 SQL을 한 곳에 모음.
 * 결과 매핑은 모두 SearchHit (type/id/title/subtitle/thumbnail) — 응답 단순화.
 *
 * 새 도메인 추가는 메서드 한 개 + XML 한 블록.
 */
@Mapper
public interface SearchMapper {

    List<SearchHit> searchChallenges(@Param("q") String q, @Param("size") int size);

    List<SearchHit> searchStores(@Param("q") String q, @Param("size") int size);

    List<SearchHit> searchPosts(@Param("q") String q, @Param("size") int size);

    List<SearchHit> searchUsers(@Param("q") String q, @Param("size") int size);
}
