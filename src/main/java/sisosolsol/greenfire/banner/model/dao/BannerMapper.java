package sisosolsol.greenfire.banner.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.banner.model.dto.BannerDTO;

import java.util.List;

@Mapper
public interface BannerMapper {

    List<BannerDTO> findActiveBanners();

    List<BannerDTO> findAllBanners();

    BannerDTO findByCode(@Param("bannerCode") Integer bannerCode);

    void insertBanner(BannerDTO banner);

    void updateBanner(BannerDTO banner);

    void deleteBanner(@Param("bannerCode") Integer bannerCode);
}
