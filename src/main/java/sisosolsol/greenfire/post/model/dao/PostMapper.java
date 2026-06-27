package sisosolsol.greenfire.post.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;
import sisosolsol.greenfire.post.model.dto.PostCreateDTO;
import sisosolsol.greenfire.post.model.dto.PostDTO;
import sisosolsol.greenfire.post.model.dto.PostUpdateDTO;
import sisosolsol.greenfire.post.model.dto.SimplePostDTO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PostMapper {
    List<SimplePostDTO> getChallengePostList(Integer challengeCode);

    PostDTO getPost(Integer postCode);

    void registChallengePost(@Param("post") PostCreateDTO post, @Param("userId") UUID userId) throws DataAccessException;

    void updatePost(@Param("postCode") Integer postCode, @Param("post") PostUpdateDTO post);

    void deletePost(Integer postCode);

    // 내가 작성한 게시글 페이징 조회
    List<SimplePostDTO> findPostsByUserCode(@Param("userCode") UUID userCode,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    int countPostsByUserCode(@Param("userCode") UUID userCode);

    // 내가 좋아요한 게시글 페이징 조회
    List<SimplePostDTO> findLikedPostsByUserCode(@Param("userCode") UUID userCode,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    int countLikedPostsByUserCode(@Param("userCode") UUID userCode);
}
