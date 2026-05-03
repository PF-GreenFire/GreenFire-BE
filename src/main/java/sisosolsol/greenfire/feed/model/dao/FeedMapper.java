package sisosolsol.greenfire.feed.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.feed.model.dto.CommentCreateParam;
import sisosolsol.greenfire.feed.model.dto.CommentDTO;
import sisosolsol.greenfire.feed.model.dto.FeedDetailDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListItemDTO;
import sisosolsol.greenfire.image.model.dto.ImageDTO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface FeedMapper {

    FeedDetailDTO getFeedDetail(@Param("postCode") Integer postCode,
                                @Param("userCode") UUID userCode);

    List<ImageDTO> getPostImages(@Param("postCode") Integer postCode);

    boolean existsLike(@Param("postCode") Integer postCode,
                       @Param("userCode") UUID userCode);

    void insertLike(@Param("postCode") Integer postCode,
                    @Param("userCode") UUID userCode);

    void deleteLike(@Param("postCode") Integer postCode,
                    @Param("userCode") UUID userCode);

    int countLike(@Param("postCode") Integer postCode);

    List<CommentDTO> getComments(@Param("postCode") Integer postCode);

    void insertComment(CommentCreateParam param);

    CommentDTO getComment(@Param("commentCode") Integer commentCode);

    UUID getCommentOwner(@Param("commentCode") Integer commentCode);

    void softDeleteComment(@Param("commentCode") Integer commentCode);

    List<FeedListItemDTO> getFeedList(@Param("userCode") UUID userCode,
                                      @Param("cursorPostCode") Integer cursorPostCode,
                                      @Param("size") int size);

    List<FeedListItemDTO> getFeaturedPosts(@Param("userCode") UUID userCode,
                                           @Param("limit") int limit);
}
