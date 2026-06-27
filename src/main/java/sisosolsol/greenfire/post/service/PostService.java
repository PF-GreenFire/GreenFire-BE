package sisosolsol.greenfire.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.InvalidForeignKeyException);
        } catch (DataAccessException e) {
            log.error("챌린지 게시글 등록 실패 (userId={}, challengeCode={})",
                    userId, post.getChallengeCode(), e);
            throw new CustomException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }

        savePostImages(post.getPostCode(), post.getImages());
        return post.getPostCode();
    }

    public void updatePost(Integer postCode, AuthUser user, PostUpdateDTO post) {
        if(!hasPermission(postCode, user))
            throw new CustomException(ExceptionCode.ACCESS_DENIED);

        postMapper.updatePost(postCode, post);
        replacePostImages(postCode, post.getImages());
    }

    private void replacePostImages(Integer postCode, List<ImageUploadDTO> images) {
        imageService.deleteAllInPost(postCode);
        savePostImages(postCode, images);
    }

    private void savePostImages(Integer postCode, List<ImageUploadDTO> images) {
        if (images == null) return;
        for (ImageUploadDTO image : images) {
            imageService.saveImage(ImageType.POST, postCode, image);
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

    @Transactional(readOnly = true)
    public Map<String, Object> getMyPosts(UUID userCode, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        List<SimplePostDTO> posts = postMapper.findPostsByUserCode(userCode, offset, safeSize);
        int totalCount = postMapper.countPostsByUserCode(userCode);

        return buildPageResponse(posts, totalCount, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMyLikedPosts(UUID userCode, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        List<SimplePostDTO> posts = postMapper.findLikedPostsByUserCode(userCode, offset, safeSize);
        int totalCount = postMapper.countLikedPostsByUserCode(userCode);

        return buildPageResponse(posts, totalCount, safePage, safeSize);
    }

    private Map<String, Object> buildPageResponse(List<SimplePostDTO> posts,
                                                  int totalCount,
                                                  int page,
                                                  int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("totalCount", totalCount);
        result.put("currentPage", page);
        result.put("hasMore", page * size < totalCount);
        return result;
    }
}
