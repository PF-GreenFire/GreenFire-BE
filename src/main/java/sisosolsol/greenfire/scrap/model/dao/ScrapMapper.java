package sisosolsol.greenfire.scrap.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.scrap.model.dto.ScrapCreateDTO;
import sisosolsol.greenfire.scrap.model.dto.ScrapFeedDTO;
import sisosolsol.greenfire.scrap.model.dto.ScrapStoreDTO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ScrapMapper {

    // 스크랩 추가. ON CONFLICT 로 idempotent. 반환값은 영향 행수 (0이면 이미 있음).
    int insertScrap(@Param("userCode") UUID userCode,
                    @Param("scrap") ScrapCreateDTO scrap);

    // 본인 소유 스크랩만 삭제. 반환값은 영향 행수 (0이면 없거나 권한 없음).
    int deleteScrap(@Param("userCode") UUID userCode,
                    @Param("scrapCode") Integer scrapCode);

    List<ScrapStoreDTO> findStoreScraps(@Param("userCode") UUID userCode);

    List<ScrapFeedDTO> findFeedScraps(@Param("userCode") UUID userCode);
}
