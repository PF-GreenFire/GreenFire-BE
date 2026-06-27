package sisosolsol.greenfire.scrap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.scrap.model.dao.ScrapMapper;
import sisosolsol.greenfire.scrap.model.dto.ScrapCreateDTO;
import sisosolsol.greenfire.scrap.model.dto.ScrapFeedDTO;
import sisosolsol.greenfire.scrap.model.dto.ScrapStoreDTO;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapService {

    private final ScrapMapper scrapMapper;

    @Transactional
    public void addScrap(UUID userCode, ScrapCreateDTO scrap) {
        scrapMapper.insertScrap(userCode, scrap);
    }

    @Transactional
    public void deleteScrap(UUID userCode, Integer scrapCode) {
        scrapMapper.deleteScrap(userCode, scrapCode);
    }

    public List<ScrapStoreDTO> getStoreScraps(UUID userCode) {
        return scrapMapper.findStoreScraps(userCode);
    }

    public List<ScrapFeedDTO> getFeedScraps(UUID userCode) {
        return scrapMapper.findFeedScraps(userCode);
    }
}
