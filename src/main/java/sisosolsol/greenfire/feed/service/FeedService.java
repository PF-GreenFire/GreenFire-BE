package sisosolsol.greenfire.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.feed.model.dao.FeedMapper;
import sisosolsol.greenfire.feed.model.dto.CommentCreateParam;
import sisosolsol.greenfire.feed.model.dto.CommentDTO;
import sisosolsol.greenfire.feed.model.dto.FeedCreateRequest;
import sisosolsol.greenfire.feed.model.dto.FeedDetailDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListItemDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListResponse;
import sisosolsol.greenfire.feed.model.dto.LikeToggleResponse;
import sisosolsol.greenfire.feed.model.dto.PostInsertParam;
import sisosolsol.greenfire.image.model.dto.ImageDTO;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.spark.service.SparkService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedMapper feedMapper;
    private final ImageService imageService;
    private final SparkService sparkService;

    public FeedDetailDTO getFeedDetail(Integer postCode, UUID userCode) {
        FeedDetailDTO detail = feedMapper.getFeedDetail(postCode, userCode);
        if (detail == null) {
            return null;
        }
        List<ImageDTO> images = feedMapper.getPostImages(postCode);
        detail.setImages(images);
        return detail;
    }

    @Transactional
    public LikeToggleResponse toggleLike(Integer postCode, UUID userCode) {
        boolean alreadyLiked = feedMapper.existsLike(postCode, userCode);
        if (alreadyLiked) {
            feedMapper.deleteLike(postCode, userCode);
        } else {
            feedMapper.insertLike(postCode, userCode);
        }
        int count = feedMapper.countLike(postCode);

        // 좋아요 추가 시 N개 단위 도달하면 작성자에게 1 spark (게시물당 캡)
        if (!alreadyLiked && count > 0 && count % SparkService.LIKE_PER_REWARD == 0) {
            FeedDetailDTO detail = feedMapper.getFeedDetail(postCode, null);
            if (detail != null && detail.getUserCode() != null
                    && !detail.getUserCode().equals(userCode) // 자기 글 자추 방지
                    && !sparkService.isLikeRewardCapped(detail.getUserCode(), postCode)) {
                sparkService.award(detail.getUserCode(), 1,
                        "LIKE_RECEIVED", "POST", postCode);
            }
        }

        return new LikeToggleResponse(!alreadyLiked, count);
    }

    public List<CommentDTO> getComments(Integer postCode) {
        return feedMapper.getComments(postCode);
    }

    @Transactional
    public CommentDTO addComment(Integer postCode, UUID userCode, String content) {
        CommentCreateParam param = new CommentCreateParam(postCode, userCode, content);
        feedMapper.insertComment(param);
        return feedMapper.getComment(param.getCommentCode());
    }

    public FeedListResponse getFeedList(UUID userCode, Integer cursorPostCode, int size) {
        // size+1을 가져와 hasMore 판정
        List<FeedListItemDTO> rows = feedMapper.getFeedList(userCode, cursorPostCode, size + 1);
        boolean hasMore = rows.size() > size;
        List<FeedListItemDTO> content = hasMore ? rows.subList(0, size) : rows;

        FeedListResponse resp = new FeedListResponse();
        resp.setContent(content);
        resp.setHasMore(hasMore);
        if (!content.isEmpty()) {
            resp.setNextCursorPostCode(content.get(content.size() - 1).getPostCode());
        }
        return resp;
    }

    public List<FeedListItemDTO> getFeaturedPosts(UUID userCode, int limit) {
        return feedMapper.getFeaturedPosts(userCode, limit);
    }

    @Transactional
    public Integer createPost(FeedCreateRequest request, List<MultipartFile> images, UUID userCode) {
        PostInsertParam param = new PostInsertParam();
        param.setUserCode(userCode);
        param.setPostContent(request.getPostContent());
        param.setPostType(request.getPostType());
        param.setStoreCode(request.getStoreCode());
        param.setChallengeCode(request.getChallengeCode());

        feedMapper.insertPost(param);
        Integer postCode = param.getPostCode();

        if (images != null && !images.isEmpty()) {
            imageService.saveImages(ImageType.POST, postCode, images);
            // 첫 이미지 path를 thumbnail로 박아둠 (피드 목록 그리드/챌린지 인증 그리드용)
            List<ImageDTO> saved = feedMapper.getPostImages(postCode);
            if (!saved.isEmpty()) {
                feedMapper.updatePostThumbnail(postCode, saved.get(0).getPath());
            }
        }

        // 인증글 작성 보상
        sparkService.award(userCode, 5, "POST_CREATED", "POST", postCode);

        return postCode;
    }

    @Transactional
    public void deleteComment(Integer commentCode, UUID userCode) {
        UUID owner = feedMapper.getCommentOwner(commentCode);
        if (owner == null) {
            return; // 이미 삭제됐거나 존재하지 않음 — idempotent
        }
        if (!owner.equals(userCode)) {
            throw new CustomException(ExceptionCode.ACCESS_DENIED);
        }
        feedMapper.softDeleteComment(commentCode);
    }
}
