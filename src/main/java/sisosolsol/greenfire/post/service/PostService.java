package sisosolsol.greenfire.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.enums.post.PostType;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.common.security.model.UserRole;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.post.model.dao.PostMapper;
import sisosolsol.greenfire.post.model.dto.PostCreateDTO;
import sisosolsol.greenfire.post.model.dto.PostDTO;
import sisosolsol.greenfire.post.model.dto.PostUpdateDTO;
import sisosolsol.greenfire.post.model.dto.SimplePostDTO;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public List<SimplePostDTO> getChallengePostList(Integer challengeCode) {
        return postMapper.getChallengePostList(challengeCode);
    }

    @Transactional(readOnly = true)
    public PostDTO getPost(Integer postCode) {
        return postMapper.getPost(postCode);
    }

    public int registChallengePost(PostCreateDTO post, UUID userId) {
        if(post.getPostType() != PostType.CHALLENGE)
            throw new BadRequestException(ExceptionCode.POST_TYPE_MISMATCH);

        try {
            postMapper.registChallengePost(post, userId);
        } catch (DataAccessException e) {
            if (e instanceof DataIntegrityViolationException) {
                throw new BadRequestException(ExceptionCode.InvalidForeignKeyException);
            } else {
                System.out.println("에러 발생" + e.getMessage());
            }
        }

        for (ImageUploadDTO image : post.getImages()) {
            imageService.saveImage(ImageType.POST, post.getPostCode(), image);
        }
        return post.getPostCode();
    }

    public void updatePost(Integer postCode, AuthUser user, PostUpdateDTO post) {
        if(!hasPermission(postCode, user))
            throw new CustomException(ExceptionCode.ACCESS_DENIED);

        postMapper.updatePost(postCode, post);

        //TODO: 추후 리팩토링
        imageService.deleteAllInPost(postCode);
        for (ImageUploadDTO image : post.getImages()) {
            imageService.saveImage(ImageType.POST, post.getPostCode(), image);
        }
    }

    public void deletePost(Integer postCode, AuthUser user) {
        if(!hasPermission(postCode, user))
            throw new CustomException(ExceptionCode.ACCESS_DENIED);

        postMapper.deletePost(postCode);
    }

    private boolean hasPermission(Integer postCode, AuthUser user) {
        PostDTO targetPost = getPost(postCode);
        return targetPost.getUserCode().equals(user.userId()) || user.role().equals(UserRole.ADMIN.name());
    }
}
