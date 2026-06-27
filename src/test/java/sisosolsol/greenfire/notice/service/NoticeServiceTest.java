package sisosolsol.greenfire.notice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.common.audit.service.ActivityLogService;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.notice.dto.response.NoticeDetailResponse;
import sisosolsol.greenfire.notice.entity.Notice;
import sisosolsol.greenfire.notice.enums.NoticeCategory;
import sisosolsol.greenfire.notice.enums.NoticeStatus;
import sisosolsol.greenfire.notice.repository.NoticeRepository;
import sisosolsol.greenfire.notice.repository.NoticeViewRepository;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeViewRepository noticeViewRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private NoticeService noticeService;

    private Notice buildNotice(Integer noticeCode, UUID authorUserCode, boolean isImportant) {
        return Notice.builder()
                .noticeCode(noticeCode)
                .noticeTitle("제목")
                .noticeContent("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .noticeStatus(NoticeStatus.ACTIVE)
                .isImportant(isImportant)
                .viewCount(0)
                .authorUserCode(authorUserCode)
                .build();
    }

    @Test
    @DisplayName("getNoticeDetail: 작성자가 존재하면 해당 닉네임을 반환한다")
    void getNoticeDetail_returnsAuthorNickname_whenUserExists() {
        // Arrange
        Integer noticeCode = 10;
        UUID authorUserCode = UUID.randomUUID();
        Notice notice = buildNotice(noticeCode, authorUserCode, false);

        UserAccount user = mock(UserAccount.class);
        when(user.getNickname()).thenReturn("작성자닉네임");

        when(noticeRepository.findByNoticeCodeAndNoticeStatus(noticeCode, NoticeStatus.ACTIVE))
                .thenReturn(Optional.of(notice));
        when(noticeRepository.findPreviousNotice(eq(NoticeStatus.ACTIVE), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(noticeRepository.findNextNotice(eq(NoticeStatus.ACTIVE), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(imageService.getImages(ImageType.NOTICE, noticeCode))
                .thenReturn(Collections.emptyList());
        when(userAccountRepository.findById(authorUserCode)).thenReturn(Optional.of(user));

        // Act
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeCode, null);

        // Assert
        assertThat(response.getAuthorName()).isEqualTo("작성자닉네임");
        assertThat(response.getNoticeCode()).isEqualTo(noticeCode);
    }

    @Test
    @DisplayName("getNoticeDetail: authorUserCode가 null이면 작성자명을 '관리자'로 반환한다")
    void getNoticeDetail_returnsDefaultName_whenAuthorIsNull() {
        // Arrange
        Integer noticeCode = 11;
        Notice notice = buildNotice(noticeCode, null, false);

        when(noticeRepository.findByNoticeCodeAndNoticeStatus(noticeCode, NoticeStatus.ACTIVE))
                .thenReturn(Optional.of(notice));
        when(noticeRepository.findPreviousNotice(eq(NoticeStatus.ACTIVE), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(noticeRepository.findNextNotice(eq(NoticeStatus.ACTIVE), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(imageService.getImages(ImageType.NOTICE, noticeCode))
                .thenReturn(Collections.emptyList());

        // Act
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeCode, null);

        // Assert
        assertThat(response.getAuthorName()).isEqualTo("관리자");
    }

    @Test
    @DisplayName("getNoticeDetail: 작성자 user를 찾을 수 없으면 '관리자' fallback")
    void getNoticeDetail_returnsDefaultName_whenUserNotFound() {
        // Arrange
        Integer noticeCode = 12;
        UUID authorUserCode = UUID.randomUUID();
        Notice notice = buildNotice(noticeCode, authorUserCode, false);

        when(noticeRepository.findByNoticeCodeAndNoticeStatus(noticeCode, NoticeStatus.ACTIVE))
                .thenReturn(Optional.of(notice));
        when(noticeRepository.findPreviousNotice(eq(NoticeStatus.ACTIVE), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(noticeRepository.findNextNotice(eq(NoticeStatus.ACTIVE), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(imageService.getImages(ImageType.NOTICE, noticeCode))
                .thenReturn(Collections.emptyList());
        when(userAccountRepository.findById(authorUserCode)).thenReturn(Optional.empty());

        // Act
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeCode, null);

        // Assert
        assertThat(response.getAuthorName()).isEqualTo("관리자");
    }

    @Test
    @DisplayName("getNoticeDetail: 공지가 없으면 NOTICE_NOT_FOUND CustomException을 던진다")
    void getNoticeDetail_throwsNotFound_whenNoticeMissing() {
        // Arrange
        Integer noticeCode = 999;
        when(noticeRepository.findByNoticeCodeAndNoticeStatus(noticeCode, NoticeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> noticeService.getNoticeDetail(noticeCode, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공지사항");
    }

    @Test
    @DisplayName("getLatestImportantNotice: 최신 중요 공지를 반환한다")
    void getLatestImportantNotice_success() {
        // Arrange
        Integer noticeCode = 20;
        Notice notice = buildNotice(noticeCode, null, true);

        when(noticeRepository.findFirstByNoticeStatusAndIsImportantOrderByCreatedAtDesc(
                NoticeStatus.ACTIVE, true)).thenReturn(Optional.of(notice));
        when(imageService.getImages(ImageType.NOTICE, noticeCode))
                .thenReturn(Collections.emptyList());

        // Act
        NoticeDetailResponse response = noticeService.getLatestImportantNotice();

        // Assert
        assertThat(response.getNoticeCode()).isEqualTo(noticeCode);
        assertThat(response.getIsImportant()).isTrue();
        assertThat(response.getAuthorName()).isEqualTo("관리자");
    }

    @Test
    @DisplayName("getLatestImportantNotice: 중요 공지가 없으면 NOTICE_NOT_FOUND")
    void getLatestImportantNotice_throwsNotFound() {
        // Arrange
        when(noticeRepository.findFirstByNoticeStatusAndIsImportantOrderByCreatedAtDesc(
                NoticeStatus.ACTIVE, true)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> noticeService.getLatestImportantNotice())
                .isInstanceOf(CustomException.class);
    }
}
