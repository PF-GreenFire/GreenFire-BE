package sisosolsol.greenfire.feed.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.challenge.service.ChallengeService;
import sisosolsol.greenfire.common.enums.post.PostType;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.feed.model.dao.FeedMapper;
import sisosolsol.greenfire.feed.model.dto.FeedCreateRequest;
import sisosolsol.greenfire.feed.model.dto.FeedDetailDTO;
import sisosolsol.greenfire.feed.model.dto.PostInsertParam;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.notification.service.NotificationService;
import sisosolsol.greenfire.spark.service.SparkService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private ImageService imageService;

    @Mock
    private SparkService sparkService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ChallengeService challengeService;

    @InjectMocks
    private FeedService feedService;

    private static final int POST = 7;
    private final UUID author = UUID.randomUUID();
    private final UUID liker = UUID.randomUUID();

    private void postOwnedByAuthor() {
        FeedDetailDTO detail = new FeedDetailDTO();
        detail.setUserCode(author);
        when(feedMapper.getFeedDetail(POST, null)).thenReturn(detail);
    }

    // ─── FD-7: 좋아요 보상은 처음 도달한 10개 단위에서만 ───

    @Test
    @DisplayName("toggleLike: 좋아요 10개에 처음 도달하면 작성자에게 1 지급")
    void like_firstMilestone_awards() {
        postOwnedByAuthor();
        when(feedMapper.existsLike(POST, liker)).thenReturn(false);
        when(feedMapper.countLike(POST)).thenReturn(10);
        when(sparkService.likeRewardCount(author, POST)).thenReturn(0);

        feedService.toggleLike(POST, liker);

        verify(sparkService).award(author, 1, "LIKE_RECEIVED", "POST", POST);
    }

    @Test
    @DisplayName("toggleLike: 취소 후 다시 눌러 같은 10개 단위에 닿으면 재지급하지 않음")
    void like_sameMilestoneAgain_doesNotAward() {
        postOwnedByAuthor();
        when(feedMapper.existsLike(POST, liker)).thenReturn(false);
        when(feedMapper.countLike(POST)).thenReturn(10);
        when(sparkService.likeRewardCount(author, POST)).thenReturn(1); // 10개 보상은 이미 받음

        feedService.toggleLike(POST, liker);

        verify(sparkService, never()).award(any(), anyInt(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("toggleLike: 20개에 처음 도달하면 두 번째 보상, 상한(10회)에 닿으면 더 지급하지 않음")
    void like_nextMilestoneAndCap() {
        postOwnedByAuthor();
        when(feedMapper.existsLike(POST, liker)).thenReturn(false);
        when(feedMapper.countLike(POST)).thenReturn(20, 110);
        when(sparkService.likeRewardCount(author, POST)).thenReturn(1, SparkService.LIKE_REWARD_CAP);

        feedService.toggleLike(POST, liker); // 20개, 보상 1회 받은 상태 → 지급
        feedService.toggleLike(POST, liker); // 110개, 이미 상한 → 미지급

        verify(sparkService, org.mockito.Mockito.times(1)).award(author, 1, "LIKE_RECEIVED", "POST", POST);
    }

    @Test
    @DisplayName("toggleLike: 자기 글에 누른 좋아요로는 보상 없음")
    void like_ownPost_noReward() {
        FeedDetailDTO detail = new FeedDetailDTO();
        detail.setUserCode(liker);
        when(feedMapper.getFeedDetail(POST, null)).thenReturn(detail);
        when(feedMapper.existsLike(POST, liker)).thenReturn(false);
        when(feedMapper.countLike(POST)).thenReturn(10);

        feedService.toggleLike(POST, liker);

        verify(sparkService, never()).award(any(), anyInt(), anyString(), anyString(), any());
    }

    // ─── FD-2: 피드 작성 시 인증 자격 ───

    @Test
    @DisplayName("createPost: CHALLENGE 글은 인증 자격이 없으면 저장하지 않음")
    void createPost_challengeWithoutEligibility_rejected() {
        FeedCreateRequest request = new FeedCreateRequest();
        request.setPostType(PostType.CHALLENGE);
        request.setChallengeCode(3);
        doThrow(new CustomException(ExceptionCode.CHALLENGE_NOT_PARTICIPATED))
                .when(challengeService).assertCanCertify(3, author);

        assertThatThrownBy(() -> feedService.createPost(request, null, author))
                .isInstanceOf(CustomException.class);
        verify(feedMapper, never()).insertPost(any());
    }

    @Test
    @DisplayName("createPost: CHALLENGE가 아닌 글에 붙은 challengeCode는 버린다")
    void createPost_nonChallengeDropsChallengeCode() {
        FeedCreateRequest request = new FeedCreateRequest();
        request.setPostType(PostType.GREENFIRE);
        request.setStoreCode(9);
        request.setChallengeCode(3);

        feedService.createPost(request, null, author);

        ArgumentCaptor<PostInsertParam> param = ArgumentCaptor.forClass(PostInsertParam.class);
        verify(feedMapper).insertPost(param.capture());
        assertThat(param.getValue().getChallengeCode()).isNull();
        verify(challengeService, never()).assertCanCertify(any(), any());
        verify(sparkService).award(eq(author), eq(5), eq("POST_CREATED"), eq("POST"), any());
    }
}
