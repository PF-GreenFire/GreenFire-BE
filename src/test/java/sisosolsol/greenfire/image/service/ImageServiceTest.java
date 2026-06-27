package sisosolsol.greenfire.image.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.common.util.FileUploadUtil;
import sisosolsol.greenfire.image.model.dao.ImageMapper;
import sisosolsol.greenfire.image.model.dto.ImageDTO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private FileUploadUtil fileUploadUtil;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("deleteImage: 이미지가 존재하면 파일과 DB를 모두 삭제한다")
    void deleteImage_deletesFileAndDb_whenImageExists() {
        // Arrange
        Integer imageCode = 100;
        ImageDTO image = mock(ImageDTO.class);
        when(image.getPath()).thenReturn("post/20240108/abc.jpg");
        when(imageMapper.findByImageCode(imageCode)).thenReturn(image);

        // Act
        imageService.deleteImage(imageCode);

        // Assert
        verify(fileUploadUtil).deleteFile("post/20240108/abc.jpg");
        verify(imageMapper).deleteByImageCode(imageCode);
    }

    @Test
    @DisplayName("deleteImage: 이미지가 없으면 아무 동작 없이 종료(no-op)한다")
    void deleteImage_noOp_whenImageMissing() {
        // Arrange
        Integer imageCode = 404;
        when(imageMapper.findByImageCode(imageCode)).thenReturn(null);

        // Act
        imageService.deleteImage(imageCode);

        // Assert
        verifyNoInteractions(fileUploadUtil);
        verify(imageMapper, never()).deleteByImageCode(imageCode);
    }

    @Test
    @DisplayName("deleteImage: 파일 삭제 후 DB 삭제 순서로 호출된다")
    void deleteImage_invokesDeleteByImageCode_onlyOnce() {
        // Arrange
        Integer imageCode = 101;
        ImageDTO image = mock(ImageDTO.class);
        when(image.getPath()).thenReturn("store/20240108/xyz.png");
        when(imageMapper.findByImageCode(imageCode)).thenReturn(image);

        // Act
        imageService.deleteImage(imageCode);

        // Assert
        verify(imageMapper).findByImageCode(imageCode);
        verify(fileUploadUtil).deleteFile("store/20240108/xyz.png");
        verify(imageMapper).deleteByImageCode(imageCode);
    }
}
