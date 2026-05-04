package sisosolsol.greenfire.banner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.banner.model.dao.BannerMapper;
import sisosolsol.greenfire.banner.model.dto.BannerDTO;
import sisosolsol.greenfire.banner.model.dto.BannerRequest;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.util.FileUploadUtil;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerMapper bannerMapper;
    private final FileUploadUtil fileUploadUtil;

    public List<BannerDTO> getActiveBanners() {
        return bannerMapper.findActiveBanners();
    }

    public List<BannerDTO> getAllBanners() {
        return bannerMapper.findAllBanners();
    }

    @Transactional
    public Integer createBanner(BannerRequest request, MultipartFile file) {
        BannerDTO dto = new BannerDTO();
        dto.setBannerTitle(request.getBannerTitle());
        dto.setLinkUrl(request.getLinkUrl());
        dto.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        dto.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);

        if (file != null && !file.isEmpty()) {
            // 별도 banner-path 추가 없이 NOTICE 폴더 재활용 (응답 path는 image_url에 그대로 저장)
            ImageUploadDTO uploaded = fileUploadUtil.uploadFile(file, ImageType.NOTICE);
            dto.setImageUrl(uploaded.getPath());
        }

        bannerMapper.insertBanner(dto);
        return dto.getBannerCode();
    }

    @Transactional
    public void updateBanner(Integer bannerCode, BannerRequest request, MultipartFile file) {
        BannerDTO dto = bannerMapper.findByCode(bannerCode);
        if (dto == null) return;

        dto.setBannerTitle(request.getBannerTitle());
        dto.setLinkUrl(request.getLinkUrl());
        if (request.getDisplayOrder() != null) dto.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) dto.setIsActive(request.getIsActive());

        if (file != null && !file.isEmpty()) {
            ImageUploadDTO uploaded = fileUploadUtil.uploadFile(file, ImageType.NOTICE);
            dto.setImageUrl(uploaded.getPath());
        }
        // 파일이 없으면 findByCode로 가져온 기존 imageUrl이 dto에 그대로 유지됨

        bannerMapper.updateBanner(dto);
    }

    @Transactional
    public void deleteBanner(Integer bannerCode) {
        bannerMapper.deleteBanner(bannerCode);
    }
}
