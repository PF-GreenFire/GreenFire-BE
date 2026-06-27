package sisosolsol.greenfire.notice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.audit.entity.ActionType;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.common.audit.service.ActivityLogService;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.image.model.dto.ImageDTO;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.notice.dto.request.NoticeCreateRequest;
import sisosolsol.greenfire.notice.dto.request.NoticeUpdateRequest;
import sisosolsol.greenfire.notice.dto.response.NoticeDetailResponse;
import sisosolsol.greenfire.notice.dto.response.NoticeListResponse;
import sisosolsol.greenfire.notice.dto.response.NoticePageResponse;
import sisosolsol.greenfire.notice.entity.Notice;
import sisosolsol.greenfire.notice.entity.NoticeView;
import sisosolsol.greenfire.notice.enums.NoticeCategory;
import sisosolsol.greenfire.notice.enums.NoticeStatus;
import sisosolsol.greenfire.notice.repository.NoticeRepository;
import sisosolsol.greenfire.notice.repository.NoticeViewRepository;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private static final String DEFAULT_AUTHOR_NAME = "관리자";

    private final NoticeRepository noticeRepository;
    private final NoticeViewRepository noticeViewRepository;
    private final ImageService imageService;
    private final ActivityLogService activityLogService;
    private final UserAccountRepository userAccountRepository;

    private String resolveAuthorName(UUID authorUserCode) {
        if (authorUserCode == null) {
            return DEFAULT_AUTHOR_NAME;
        }
        return userAccountRepository.findById(authorUserCode)
                .map(UserAccount::getNickname)
                .orElse(DEFAULT_AUTHOR_NAME);
    }

    /**
     * 공지사항 목록 조회 (페이징, 필터링, 검색)
     */
    public NoticePageResponse getNoticeList(Integer page, Integer limit, String category,
                                            String searchKeyword, UUID userCode) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        NoticeCategory noticeCategory = category != null && !category.equals("ALL")
                ? NoticeCategory.valueOf(category)
                : null;

        Page<Notice> noticePage = noticeRepository.findNoticesWithFilters(
                NoticeStatus.ACTIVE,
                noticeCategory,
                searchKeyword,
                pageable
        );

        Page<NoticeListResponse> responsePage = noticePage.map(notice -> {
            Boolean isViewed = userCode != null
                    && noticeViewRepository.existsByNotice_NoticeCodeAndUserCode(
                    notice.getNoticeCode(), userCode);

            // ⭐ 이미지 존재 여부 확인 (MyBatis)
            Boolean hasImages = imageService.hasImages(ImageType.NOTICE, notice.getNoticeCode());

            return NoticeListResponse.from(notice, isViewed, hasImages);
        });

        return NoticePageResponse.from(responsePage);
    }

    /**
     * 공지사항 상세 조회
     */
    public NoticeDetailResponse getNoticeDetail(Integer noticeCode, UUID userCode) {
        // ⭐ 수정: findByIdWithAttachments → findByNoticeCodeAndNoticeStatus
        Notice notice = noticeRepository.findByNoticeCodeAndNoticeStatus(noticeCode, NoticeStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        Boolean isViewed = userCode != null
                && noticeViewRepository.existsByNotice_NoticeCodeAndUserCode(noticeCode, userCode);

        // ⭐ 이미지 조회 (MyBatis)
        List<ImageDTO> images = imageService.getImages(ImageType.NOTICE, noticeCode);

        // 이전/다음 공지사항 조회
        Notice prevNotice = noticeRepository.findPreviousNotice(
                NoticeStatus.ACTIVE,
                notice.getNoticeCategory(),
                notice.getCreatedAt(),
                PageRequest.of(0, 1)
        ).stream().findFirst().orElse(null);

        Notice nextNotice = noticeRepository.findNextNotice(
                NoticeStatus.ACTIVE,
                notice.getNoticeCategory(),
                notice.getCreatedAt(),
                PageRequest.of(0, 1)
        ).stream().findFirst().orElse(null);

        String authorName = resolveAuthorName(notice.getAuthorUserCode());

        return NoticeDetailResponse.from(notice, authorName, isViewed, images, prevNotice, nextNotice);
    }

    private static final int VIEW_DUPLICATE_HOURS = 24;

    /**
     * 조회수 증가 (24시간 내 중복 방지)
     * - 로그인 사용자: userCode 기반
     * - 비로그인 사용자: IP 기반
     */
    @Transactional
    public void incrementViewCount(Integer noticeCode, UUID userCode, String ipAddress) {
        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        LocalDateTime since = LocalDateTime.now().minusHours(VIEW_DUPLICATE_HOURS);

        // 24시간 내 중복 조회 체크
        boolean alreadyViewed;
        NoticeView noticeView;

        if (userCode != null) {
            // 로그인 사용자: userCode 기반 체크
            alreadyViewed = noticeViewRepository.existsByNoticeCodeAndUserCodeSince(
                    noticeCode, userCode, since);
            noticeView = NoticeView.ofUser(notice, userCode, ipAddress);
        } else {
            // 비로그인 사용자: IP 기반 체크
            alreadyViewed = noticeViewRepository.existsByNoticeCodeAndIpAddressSince(
                    noticeCode, ipAddress, since);
            noticeView = NoticeView.ofGuest(notice, ipAddress);
        }

        if (alreadyViewed) {
            return; // 24시간 내 이미 조회함
        }

        // 조회 기록 저장
        noticeViewRepository.save(noticeView);

        // 조회수 증가
        notice.incrementViewCount();
    }

    /**
     * 관련 공지사항 조회
     */
    public List<NoticeListResponse> getRelatedNotices(Integer noticeCode, Integer limit) {
        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, limit);
        List<Notice> relatedNotices = noticeRepository.findRelatedNotices(
                NoticeStatus.ACTIVE,
                notice.getNoticeCategory(),
                noticeCode,
                pageable
        );

        return relatedNotices.stream()
                .map(n -> {
                    Boolean hasImages = imageService.hasImages(ImageType.NOTICE, n.getNoticeCode());
                    return NoticeListResponse.from(n, false, hasImages);
                })
                .collect(Collectors.toList());
    }

    /**
     * 최신 중요 공지사항 조회 (미리보기용)
     */
    public NoticeDetailResponse getLatestImportantNotice() {
        Notice notice = noticeRepository.findFirstByNoticeStatusAndIsImportantOrderByCreatedAtDesc(
                NoticeStatus.ACTIVE, true
        ).orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        // ⭐ 이미지 조회
        List<ImageDTO> images = imageService.getImages(ImageType.NOTICE, notice.getNoticeCode());

        String authorName = resolveAuthorName(notice.getAuthorUserCode());
        return NoticeDetailResponse.from(notice, authorName, false, images, null, null);
    }

    /**
     * 공지사항 생성 (관리자)
     */
    @Transactional
    public Integer createNotice(NoticeCreateRequest request, UUID authorUserCode,
                                List<MultipartFile> files, String ipAddress) {
        Notice notice = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticeCategory(request.getNoticeCategory())
                .isImportant(request.getIsImportant())
                .authorUserCode(authorUserCode)
                .thumbnailUrl(request.getThumbnailUrl())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        if (files != null && !files.isEmpty()) {
            imageService.saveImages(ImageType.NOTICE, savedNotice.getNoticeCode(), files);
        }

        activityLogService.log(authorUserCode, ActionType.CREATE, ResourceType.NOTICE,
                savedNotice.getNoticeCode().toString(), request.getNoticeTitle(), ipAddress);

        return savedNotice.getNoticeCode();
    }

    /**
     * 공지사항 수정 (관리자)
     */
    @Transactional
    public void updateNotice(Integer noticeCode, NoticeUpdateRequest request,
                             List<MultipartFile> files, UUID userId, String ipAddress) {
        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        notice.updateNotice(
                request.getNoticeTitle(),
                request.getNoticeContent(),
                request.getNoticeCategory(),
                request.getIsImportant(),
                request.getThumbnailUrl(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (files != null && !files.isEmpty()) {
            imageService.deleteAllImages(ImageType.NOTICE, noticeCode);
            imageService.saveImages(ImageType.NOTICE, noticeCode, files);
        }

        activityLogService.log(userId, ActionType.UPDATE, ResourceType.NOTICE,
                noticeCode.toString(), request.getNoticeTitle(), ipAddress);
    }

    /**
     * 공지사항 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteNotice(Integer noticeCode, UUID userId, String ipAddress) {
        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        String snapshot = notice.getNoticeTitle();
        notice.delete();
        imageService.deleteAllImages(ImageType.NOTICE, noticeCode);

        activityLogService.log(userId, ActionType.DELETE, ResourceType.NOTICE,
                noticeCode.toString(), snapshot, ipAddress);
    }
}