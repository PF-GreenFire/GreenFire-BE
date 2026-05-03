package sisosolsol.greenfire.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.feed.model.dao.FeedMapper;
import sisosolsol.greenfire.feed.model.dto.CommentCreateParam;
import sisosolsol.greenfire.feed.model.dto.CommentDTO;
import sisosolsol.greenfire.feed.model.dto.FeedDetailDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListItemDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListResponse;
import sisosolsol.greenfire.feed.model.dto.LikeToggleResponse;
import sisosolsol.greenfire.image.model.dto.ImageDTO;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedMapper feedMapper;

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
