package sisosolsol.greenfire.notice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeViewRepository noticeViewRepository;
    private final ImageService imageService;  // ⭐ ImageService 추가

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

        // TODO: authorName은 User 엔티티에서 가져와야 함 (현재는 임시)
        String authorName = "관리자";

        return NoticeDetailResponse.from(notice, authorName, isViewed, images, prevNotice, nextNotice);
    }

    /**
     * 조회수 증가 (중복 방지)
     */
    @Transactional
    public void incrementViewCount(Integer noticeCode, UUID userCode) {
        if (userCode == null) {
            return; // 비로그인 사용자는 조회수 증가 안함
        }

        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        // 중복 조회 체크
        if (noticeViewRepository.existsByNotice_NoticeCodeAndUserCode(noticeCode, userCode)) {
            return; // 이미 조회한 사용자
        }

        // 조회 기록 저장
        NoticeView noticeView = NoticeView.builder()
                .notice(notice)
                .userCode(userCode)
                .build();
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

        String authorName = "관리자";
        return NoticeDetailResponse.from(notice, authorName, false, images, null, null);
    }

    /**
     * 공지사항 생성 (관리자)
     */
    @Transactional
    public Integer createNotice(NoticeCreateRequest request, UUID authorUserCode,
                                List<MultipartFile> files) {
        // 1. Notice 저장
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

        // 2. ⭐ 이미지 저장 (ImageService 활용)
        if (files != null && !files.isEmpty()) {
            imageService.saveImages(ImageType.NOTICE, savedNotice.getNoticeCode(), files);
        }

        return savedNotice.getNoticeCode();
    }

    /**
     * 공지사항 수정 (관리자)
     */
    @Transactional
    public void updateNotice(Integer noticeCode, NoticeUpdateRequest request,
                             List<MultipartFile> files) {
        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        // 1. Notice 수정
        notice.updateNotice(
                request.getNoticeTitle(),
                request.getNoticeContent(),
                request.getNoticeCategory(),
                request.getIsImportant(),
                request.getThumbnailUrl(),
                request.getStartDate(),
                request.getEndDate()
        );

        // 2. ⭐ 기존 이미지 삭제 후 새로 등록
        if (files != null && !files.isEmpty()) {
            imageService.deleteAllImages(ImageType.NOTICE, noticeCode);
            imageService.saveImages(ImageType.NOTICE, noticeCode, files);
        }
    }

    /**
     * 공지사항 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteNotice(Integer noticeCode) {
        Notice notice = noticeRepository.findById(noticeCode)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTICE_NOT_FOUND));

        // 1. Notice 소프트 삭제
        notice.delete();

        // 2. ⭐ 이미지 삭제 (파일 + DB)
        imageService.deleteAllImages(ImageType.NOTICE, noticeCode);
    }
}