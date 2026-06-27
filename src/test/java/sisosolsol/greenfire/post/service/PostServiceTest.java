package sisosolsol.greenfire.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import sisosolsol.greenfire.common.enums.post.PostType;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.common.security.model.UserRole;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.post.model.dao.PostMapper;
import sisosolsol.greenfire.post.model.dto.PostCreateDTO;
import sisosolsol.greenfire.post.model.dto.PostDTO;
import sisosolsol.greenfire.post.model.dto.PostUpdateDTO;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("registChallengePost: DataIntegrityViolationException 발생 시 BadRequestException(InvalidForeignKey)을 던진다")
    void registChallengePost_throwsBadRequest_onDataIntegrityViolation() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PostCreateDTO post = mock(PostCreateDTO.class);
        when(post.getPostType()).thenReturn(PostType.CHALLENGE);
        doThrow(new DataIntegrityViolationException("fk fail"))
                .when(postMapper).registChallengePost(eq(post), eq(userId));

        // Act + Assert
        assertThatThrownBy(() -> postService.registChallengePost(post, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("외래 키");
    }

    @Test
    @DisplayName("registChallengePost: DataAccessException 발생 시 DATABASE_ACCESS_ERROR CustomException을 던진다")
    void registChallengePost_throwsDbAccessError_onDataAccessException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PostCreateDTO post = mock(PostCreateDTO.class);
        when(post.getPostType()).thenReturn(PostType.CHALLENGE);
        doThrow(new DataAccessException("db error") {})
                .when(postMapper).registChallengePost(eq(post), eq(userId));

        // Act + Assert
        assertThatThrownBy(() -> postService.registChallengePost(post, userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("데이터베이스");
    }

    @Test
    @DisplayName("registChallengePost: postType이 CHALLENGE가 아니면 POST_TYPE_MISMATCH BadRequestException")
    void registChallengePost_throwsBadRequest_whenPostTypeMismatch() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PostCreateDTO post = mock(PostCreateDTO.class);
        when(post.getPostType()).thenReturn(PostType.DEFAULT);

        // Act + Assert
        assertThatThrownBy(() -> postService.registChallengePost(post, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("게시물 타입");
    }

    @Test
    @DisplayName("updatePost: 본인이 작성한 게시물이 아니고 ADMIN도 아니면 ACCESS_DENIED 예외를 던진다")
    void updatePost_throwsAccessDenied_whenNotOwnerAndNotAdmin() {
        // Arrange
        Integer postCode = 50;
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        AuthUser user = new AuthUser(otherUserId, "x@y.z", UserRole.USER.name());
        PostUpdateDTO update = mock(PostUpdateDTO.class);

        PostDTO target = mock(PostDTO.class);
        when(target.getUserCode()).thenReturn(ownerId);
        when(postMapper.getPost(postCode)).thenReturn(target);

        // Act + Assert
        assertThatThrownBy(() -> postService.updatePost(postCode, user, update))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("허가");
    }

    @Test
    @DisplayName("updatePost: 작성자 본인이면 정상적으로 수정 및 이미지 교체가 호출된다")
    void updatePost_success_whenOwner() {
        // Arrange
        Integer postCode = 51;
        UUID ownerId = UUID.randomUUID();

        AuthUser user = new AuthUser(ownerId, "owner@y.z", UserRole.USER.name());
        PostUpdateDTO update = mock(PostUpdateDTO.class);
        when(update.getImages()).thenReturn(null);

        PostDTO target = mock(PostDTO.class);
        when(target.getUserCode()).thenReturn(ownerId);
        when(postMapper.getPost(postCode)).thenReturn(target);

        // Act
        postService.updatePost(postCode, user, update);

        // Assert
        verify(postMapper).updatePost(postCode, update);
        verify(imageService).deleteAllInPost(postCode);
    }

    @Test
    @DisplayName("updatePost: ADMIN 권한이면 다른 사용자의 게시물도 수정 가능")
    void updatePost_success_whenAdmin() {
        // Arrange
        Integer postCode = 52;
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        AuthUser admin = new AuthUser(adminId, "admin@y.z", UserRole.ADMIN.name());
        PostUpdateDTO update = mock(PostUpdateDTO.class);
        when(update.getImages()).thenReturn(null);

        PostDTO target = mock(PostDTO.class);
        when(target.getUserCode()).thenReturn(ownerId);
        when(postMapper.getPost(postCode)).thenReturn(target);

        // Act
        postService.updatePost(postCode, admin, update);

        // Assert
        verify(postMapper).updatePost(postCode, update);
    }
}
