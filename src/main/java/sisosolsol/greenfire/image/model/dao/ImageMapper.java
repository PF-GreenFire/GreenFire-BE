package sisosolsol.greenfire.image.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.image.model.dto.ImageDTO;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;

import java.util.List;

@Mapper
public interface ImageMapper {
    void savePostImage(@Param("postCode") Integer code, @Param("image") ImageUploadDTO image);

    void saveStoreImage(@Param("storeCode") Integer code, @Param("image") ImageUploadDTO image);

    void deleteAllInPost(Integer postCode);

    void deleteAllInStore(int storeCode);

    void saveImage(@Param("referenceType") String referenceType,
                   @Param("referenceCode") Integer referenceCode,
                   @Param("image") ImageUploadDTO image);

    List<ImageDTO> findByReference(@Param("referenceType") String referenceType,
                                   @Param("referenceCode") Integer referenceCode);

    void deleteAllByReference(@Param("referenceType") String referenceType,
                              @Param("referenceCode") Integer referenceCode);

    void deleteByImageCode(Integer imageCode);

    int countByReference(@Param("referenceType") String referenceType,
                         @Param("referenceCode") Integer referenceCode);
}
