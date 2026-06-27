package sisosolsol.greenfire.user.dao;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.user.dto.FriendDTO;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UpdateUserCommand;
import sisosolsol.greenfire.user.dto.User;

import java.util.UUID;

@Mapper
public interface UserMapper {

    User findUserSummary(@Param("userCode") UUID userCode);

    // 회원 프로필 정보 조회
    User findByUserCode(@Param("userCode") UUID userCode);

    // 회원 프로필 정보 수정
    void updateUserProfile(UpdateUserCommand command);

    ScrapbookSummaryDTO getScrapbookSummary(@Param("userCode") UUID userCode);

    List<ChallengeDTO> getChallengeSummary(@Param("userCode") UUID userCode);

    int countParticipatingChallenge(UUID userCode);

    void changePassword(@Param("userCode") UUID userCode, @Param("password") String password);

    void followUser(@Param("userCode") UUID userCode, @Param("targetUser") UUID targetUser);

    void deleteFollow(@Param("userCode") UUID userCode, @Param("targetUser") UUID targetUser);

    void changeCoverImage(@Param("userCode") UUID userCode, @Param("storageKey") String storageKey);

    List<ChallengeDTO> getScrapChallenges(@Param("userCode") UUID userCode);

    List<FriendDTO> getScrapFriends(UUID userCode);

    String findImagePathByImageCode(@Param("imageCode") int imageCode);

    sisosolsol.greenfire.user.dto.PublicProfileRow findPublicProfile(
            @Param("targetCode") UUID targetCode,
            @Param("viewerCode") UUID viewerCode);

    int countFollowers(@Param("userCode") UUID userCode);

    int countFollowings(@Param("userCode") UUID userCode);

    List<ChallengeDTO> findMyChallenges(@Param("userCode") UUID userCode,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);
}