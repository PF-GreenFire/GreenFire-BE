package sisosolsol.greenfire.report.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.challenge.model.dao.ChallengeMapper;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.common.audit.service.ActivityLogService;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.feed.model.dao.FeedMapper;
import sisosolsol.greenfire.notification.service.NotificationService;
import sisosolsol.greenfire.post.model.dao.PostMapper;
import sisosolsol.greenfire.post.model.dto.PostDTO;
import sisosolsol.greenfire.report.dto.ReportCreateRequest;
import sisosolsol.greenfire.report.dto.ReportHandleRequest;
import sisosolsol.greenfire.report.entity.Report;
import sisosolsol.greenfire.report.enums.ReportCategory;
import sisosolsol.greenfire.report.enums.ReportStatus;
import sisosolsol.greenfire.report.repository.ReportRepository;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PostMapper postMapper;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private ChallengeMapper challengeMapper;

    @InjectMocks
    private ReportService reportService;

    private ReportCreateRequest request(ResourceType type, String id) {
        ReportCreateRequest req = mock(ReportCreateRequest.class);
        when(req.getResourceType()).thenReturn(type);
        when(req.getResourceId()).thenReturn(id);
        return req;
    }

    @Test
    @DisplayName("createReport: 본인 게시글 신고는 CANNOT_REPORT_OWN_CONTENT")
    void createReport_rejectsOwnPost() {
        UUID me = UUID.randomUUID();
        PostDTO post = mock(PostDTO.class);
        when(post.getUserCode()).thenReturn(me);
        when(postMapper.getPost(10)).thenReturn(post);

        ReportCreateRequest req = request(ResourceType.POST, "10");

        assertThatThrownBy(() -> reportService.createReport(req, me, "127.0.0.1"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인");
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReport: 존재하지 않는 댓글이나 숫자가 아닌 ID는 REPORT_TARGET_NOT_FOUND")
    void createReport_rejectsMissingTarget() {
        when(feedMapper.getCommentOwner(7)).thenReturn(null);
        ReportCreateRequest missing = request(ResourceType.COMMENT, "7");
        ReportCreateRequest garbage = request(ResourceType.CHALLENGE, "abc");

        assertThatThrownBy(() -> reportService.createReport(missing, UUID.randomUUID(), "ip"))
                .hasMessageContaining("신고 대상");
        assertThatThrownBy(() -> reportService.createReport(garbage, UUID.randomUUID(), "ip"))
                .hasMessageContaining("신고 대상");
    }

    @Test
    @DisplayName("handleReport(HANDLED_DELETED): 신고된 게시글을 실제로 소프트 삭제한다")
    void handleReport_deleted_hidesPost() {
        Report report = Report.builder()
                .reporterId(UUID.randomUUID())
                .resourceType(ResourceType.POST)
                .resourceId("10")
                .category(ReportCategory.SPAM)
                .reason("광고")
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        ReportHandleRequest req = mock(ReportHandleRequest.class);
        when(req.getStatus()).thenReturn(ReportStatus.HANDLED_DELETED);

        reportService.handleReport(1L, req, UUID.randomUUID(), "ip");

        verify(postMapper).deletePost(10);
    }

    @Test
    @DisplayName("handleReport(DISMISSED): 콘텐츠는 건드리지 않는다")
    void handleReport_dismissed_keepsContent() {
        Report report = Report.builder()
                .reporterId(UUID.randomUUID())
                .resourceType(ResourceType.COMMENT)
                .resourceId("3")
                .category(ReportCategory.OTHER)
                .reason("기타")
                .build();
        when(reportRepository.findById(2L)).thenReturn(Optional.of(report));
        ReportHandleRequest req = mock(ReportHandleRequest.class);
        when(req.getStatus()).thenReturn(ReportStatus.DISMISSED);

        reportService.handleReport(2L, req, UUID.randomUUID(), "ip");

        verify(feedMapper, never()).softDeleteComment(any());
        verify(postMapper, never()).deletePost(any());
    }
}
