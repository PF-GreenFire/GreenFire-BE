package sisosolsol.greenfire.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.util.FileUploadUtil;
import sisosolsol.greenfire.image.model.dao.ImageMapper;
import sisosolsol.greenfire.image.model.dto.ImageDTO;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;
import sisosolsol.greenfire.common.enums.image.ImageType;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {
    private final ImageMapper imageMapper;
    private final FileUploadUtil fileUploadUtil;

    public void saveImage(ImageType imageType, Integer code, ImageUploadDTO image) {
        switch(imageType) {
            case STORE: imageMapper.saveStoreImage(code, image);
                break;
            case POST: imageMapper.savePostImage(code, image); break;
        }
    }

    public void deleteAllInPost(Integer postCode) {
        imageMapper.deleteAllInPost(postCode);
    }

    public void deleteAllInStore(int storeCode) { imageMapper.deleteAllInStore(storeCode);}

    public void saveImages(ImageType imageType, Integer referenceCode, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            // 1. 파일 업로드 (날짜 폴더 + UUID 방식)
            ImageUploadDTO imageDTO = fileUploadUtil.uploadFile(file, imageType);

            // 2. DB 저장
            imageMapper.saveImage(imageType.name(), referenceCode, imageDTO);
        }
    }

    /**
     * 단일 이미지 저장
     */
    public ImageUploadDTO saveImage(ImageType imageType, Integer referenceCode, MultipartFile file) {
        // 1. 파일 업로드
        ImageUploadDTO imageDTO = fileUploadUtil.uploadFile(file, imageType);

        // 2. DB 저장
        imageMapper.saveImage(imageType.name(), referenceCode, imageDTO);

        return imageDTO;
    }

    /**
     * 이미지 조회
     *
     * @param imageType 이미지 타입
     * @param referenceCode 참조 코드
     * @return 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<ImageDTO> getImages(ImageType imageType, Integer referenceCode) {
        return imageMapper.findByReference(imageType.name(), referenceCode);
    }

    /**
     * 이미지 전체 삭제 (파일 + DB)
     *
     * @param imageType 이미지 타입
     * @param referenceCode 참조 코드
     */
    public void deleteAllImages(ImageType imageType, Integer referenceCode) {
        // 1. 파일 경로 조회
        List<ImageDTO> images = imageMapper.findByReference(imageType.name(), referenceCode);

        // 2. 파일 삭제
        for (ImageDTO image : images) {
            fileUploadUtil.deleteFile(image.getPath());
        }

        // 3. DB 삭제
        imageMapper.deleteAllByReference(imageType.name(), referenceCode);
    }

    public void deleteImage(Integer imageCode) {
        ImageDTO image = imageMapper.findByImageCode(imageCode);
        if (image == null) {
            return;
        }
        fileUploadUtil.deleteFile(image.getPath());
        imageMapper.deleteByImageCode(imageCode);
    }

    /**
     * 이미지 존재 여부 확인
     *
     * @param imageType 이미지 타입
     * @param referenceCode 참조 코드
     * @return 이미지가 있으면 true
     */
    @Transactional(readOnly = true)
    public boolean hasImages(ImageType imageType, Integer referenceCode) {
        return imageMapper.countByReference(imageType.name(), referenceCode) > 0;
    }
}
