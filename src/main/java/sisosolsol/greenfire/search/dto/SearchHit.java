package sisosolsol.greenfire.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 검색 결과 단일 항목. 도메인 마다 다른 필드가 있지만 응답을 단순화하기 위해 핵심만 평탄화.
 *
 * - id: 도메인 PK (UUID나 Integer를 모두 String으로 통일)
 * - title: 화면에 보일 메인 텍스트 (challenge.title / store.name / post 첫 40자 / user.nickname)
 * - subtitle: 보조 텍스트 (카테고리·주소·작성자·등급 등)
 * - thumbnail: 썸네일 path 또는 이모지
 */
@Getter
@Setter
@Builder
public class SearchHit {
    private String type;       // CHALLENGE / STORE / POST / USER
    private String id;
    private String title;
    private String subtitle;
    private String thumbnail;
}
