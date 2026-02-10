package sisosolsol.greenfire.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.image.model.dao.ImageMapper;
import sisosolsol.greenfire.image.model.dto.ImageDTO;
import sisosolsol.greenfire.notice.dto.request.NoticeCreateRequest;
import sisosolsol.greenfire.notice.entity.Notice;
import sisosolsol.greenfire.notice.enums.NoticeCategory;
import sisosolsol.greenfire.notice.enums.NoticeStatus;
import sisosolsol.greenfire.notice.repository.NoticeRepository;
import sisosolsol.greenfire.notice.repository.NoticeViewRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 공지사항 통합 테스트 (Image 통합 버전)
 *
 * 수정 사항:
 * - NoticeAttachment 제거, Image 통합
 * - 이미지 업로드/조회 테스트 추가
 * - hasAttachments → hasImages로 변경
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoticeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeViewRepository noticeViewRepository;

    @Autowired
    private ImageMapper imageMapper;

    private static UUID testUserCode;
    private static Integer testNoticeCode1;
    private static Integer testNoticeCode2;
    private static Integer testImportantNoticeCode;
    private static Integer testNoticeWithImages;

    @BeforeAll
    static void beforeAll() {
        testUserCode = UUID.randomUUID();
    }

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        noticeViewRepository.deleteAll();
        imageMapper.deleteAllByReference("NOTICE", testNoticeCode1);
        noticeRepository.deleteAll();

        // 테스트 공지사항 생성
        createTestNotices();
    }

    private void createTestNotices() {
        // 1. 일반 공지사항 1
        Notice notice1 = Notice.builder()
                .noticeTitle("테스트 공지사항 1")
                .noticeContent("이것은 테스트 공지사항 1의 내용입니다.")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .build();
        testNoticeCode1 = noticeRepository.save(notice1).getNoticeCode();

        // 2. 이벤트 공지사항
        Notice notice2 = Notice.builder()
                .noticeTitle("테스트 이벤트 공지")
                .noticeContent("이것은 테스트 이벤트 공지의 내용입니다.")
                .noticeCategory(NoticeCategory.EVENT)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .build();
        testNoticeCode2 = noticeRepository.save(notice2).getNoticeCode();

        // 3. 중요 공지사항
        Notice importantNotice = Notice.builder()
                .noticeTitle("중요 공지사항")
                .noticeContent("이것은 중요한 공지사항입니다.")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(true)
                .authorUserCode(testUserCode)
                .build();
        testImportantNoticeCode = noticeRepository.save(importantNotice).getNoticeCode();

        // 4. 검색 테스트용 공지사항
        Notice searchNotice = Notice.builder()
                .noticeTitle("검색 키워드 테스트")
                .noticeContent("이 공지사항은 검색 기능을 테스트하기 위한 것입니다.")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .build();
        noticeRepository.save(searchNotice);

        // 5. 삭제된 공지사항 (조회되지 않아야 함)
        Notice deletedNotice = Notice.builder()
                .noticeTitle("삭제된 공지사항")
                .noticeContent("이 공지사항은 삭제되었습니다.")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .build();
        Notice saved = noticeRepository.save(deletedNotice);
        saved.delete();
        noticeRepository.save(saved);
    }

    // ========== 1. 공지사항 목록 조회 테스트 ==========

    @Test
    @Order(1)
    @DisplayName("1-1. 전체 공지사항 목록 조회 - 성공")
    @WithMockUser
    void getAllNoticeList_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    @Order(2)
    @DisplayName("1-2. 중요 공지사항 상단 고정 확인")
    @WithMockUser
    void getNoticeList_ImportantNoticeFirst() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices[0].isImportant").value(true))
                .andExpect(jsonPath("$.notices[0].noticeTitle").value("중요 공지사항"))
                .andReturn();

        System.out.println("응답: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(3)
    @DisplayName("1-3. 페이지네이션 테스트")
    @WithMockUser
    void getNoticeList_Pagination() throws Exception {
        // 첫 페이지
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.notices.length()").value(2))
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.hasMore").value(true));

        // 두 번째 페이지
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "2")
                        .param("limit", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.notices.length()").value(2))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    // ========== 2. 필터링 테스트 ==========

    @Test
    @Order(4)
    @DisplayName("2-1. 공지사항 카테고리 필터링")
    @WithMockUser
    void getNoticeList_FilterByNoticeCategory() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20")
                        .param("category", "NOTICE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.notices[0].noticeCategory").value("NOTICE"));
    }

    @Test
    @Order(5)
    @DisplayName("2-2. 이벤트 카테고리 필터링")
    @WithMockUser
    void getNoticeList_FilterByEventCategory() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20")
                        .param("category", "EVENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notices[0].noticeCategory").value("EVENT"));
    }

    @Test
    @Order(6)
    @DisplayName("2-3. 삭제된 공지사항은 조회되지 않음")
    @WithMockUser
    void getNoticeList_DeletedNoticeNotShown() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).doesNotContain("삭제된 공지사항");
    }

    // ========== 3. 검색 기능 테스트 ==========

    @Test
    @Order(7)
    @DisplayName("3-1. 제목으로 검색 - 성공")
    @WithMockUser
    void searchNoticeByTitle_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20")
                        .param("searchKeyword", "검색"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notices[0].noticeTitle").value("검색 키워드 테스트"));
    }

    @Test
    @Order(8)
    @DisplayName("3-2. 내용으로 검색 - 성공")
    @WithMockUser
    void searchNoticeByContent_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20")
                        .param("searchKeyword", "기능을 테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @Order(9)
    @DisplayName("3-3. 검색 결과 없음")
    @WithMockUser
    void searchNotice_NoResults() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20")
                        .param("searchKeyword", "존재하지않는키워드"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.notices.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    // ========== 4. 공지사항 상세 조회 테스트 ==========

    @Test
    @Order(10)
    @DisplayName("4-1. 공지사항 상세 조회 - 성공")
    @WithMockUser
    void getNoticeDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notices/{noticeCode}", testNoticeCode1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeCode").value(testNoticeCode1))
                .andExpect(jsonPath("$.noticeTitle").value("테스트 공지사항 1"))
                .andExpect(jsonPath("$.noticeContent").value("이것은 테스트 공지사항 1의 내용입니다."))
                .andExpect(jsonPath("$.noticeCategory").value("NOTICE"))
                .andExpect(jsonPath("$.viewCount").exists())
                .andExpect(jsonPath("$.images").isArray())  // ⭐ 이미지 배열 확인
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @Order(11)
    @DisplayName("4-2. 존재하지 않는 공지사항 조회 - 실패")
    @WithMockUser
    void getNoticeDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/notices/{noticeCode}", 99999))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    // ========== 5. 조회수 증가 테스트 ==========

    @Test
    @Order(12)
    @DisplayName("5-1. 최초 조회 시 조회수 증가")
    @WithMockUser
    @Transactional
    void incrementViewCount_FirstView() throws Exception {
        UUID newUserCode = UUID.randomUUID();
        Notice noticeBefore = noticeRepository.findById(testNoticeCode1).orElseThrow();
        int viewCountBefore = noticeBefore.getViewCount();

        mockMvc.perform(post("/api/v1/notices/{noticeCode}/view", testNoticeCode1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\": \"" + newUserCode + "\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Notice noticeAfter = noticeRepository.findById(testNoticeCode1).orElseThrow();
        assertThat(noticeAfter.getViewCount()).isEqualTo(viewCountBefore + 1);
    }

    @Test
    @Order(13)
    @DisplayName("5-2. 중복 조회 시 조회수 증가 안함")
    @WithMockUser
    @Transactional
    void incrementViewCount_DuplicateView() throws Exception {
        UUID newUserCode = UUID.randomUUID();

        // 첫 번째 조회
        mockMvc.perform(post("/api/v1/notices/{noticeCode}/view", testNoticeCode1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\": \"" + newUserCode + "\"}"))
                .andExpect(status().isOk());

        Notice noticeAfterFirstView = noticeRepository.findById(testNoticeCode1).orElseThrow();
        int viewCountAfterFirst = noticeAfterFirstView.getViewCount();

        // 두 번째 조회 (중복)
        mockMvc.perform(post("/api/v1/notices/{noticeCode}/view", testNoticeCode1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\": \"" + newUserCode + "\"}"))
                .andExpect(status().isOk());

        Notice noticeAfterSecondView = noticeRepository.findById(testNoticeCode1).orElseThrow();
        assertThat(noticeAfterSecondView.getViewCount()).isEqualTo(viewCountAfterFirst);
    }

    // ========== 6. 이미지 업로드 테스트 ⭐ 새로 추가 ==========

    @Test
    @Order(14)
    @DisplayName("6-1. 공지사항 생성 (이미지 포함) - 성공")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void createNoticeWithImages_Success() throws Exception {
        // given
        NoticeCreateRequest request = new NoticeCreateRequest(
                "이미지 포함 공지사항",
                "이미지가 포함된 공지사항입니다.",
                NoticeCategory.NOTICE,
                false,
                null,
                null,
                null
        );

        MockMultipartFile noticeData = new MockMultipartFile(
                "notice",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "test1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "test2.png",
                "image/png",
                "test image content 2".getBytes()
        );

        // when & then
        MvcResult result = mockMvc.perform(multipart("/api/v1/notices")
                        .file(noticeData)
                        .file(image1)
                        .file(image2)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.noticeCode").exists())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Integer createdNoticeCode = objectMapper.readTree(responseContent).get("noticeCode").asInt();

        // 이미지 저장 확인
        List<ImageDTO> images = imageMapper.findByReference("NOTICE", createdNoticeCode);
        assertThat(images).hasSize(2);
        assertThat(images.get(0).getOriginName()).isEqualTo("test1.jpg");
        assertThat(images.get(1).getOriginName()).isEqualTo("test2.png");
    }

    @Test
    @Order(15)
    @DisplayName("6-2. 이미지 포함 공지사항 상세 조회")
    @WithMockUser
    @Transactional
    void getNoticeDetailWithImages() throws Exception {
        // given - 이미지 포함 공지사항 생성
        Notice notice = Notice.builder()
                .noticeTitle("이미지 테스트")
                .noticeContent("이미지가 있는 공지사항")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .build();
        Integer noticeCode = noticeRepository.save(notice).getNoticeCode();

        // 이미지 직접 삽입 (MyBatis)
        imageMapper.saveImage("NOTICE", noticeCode, createTestImageDTO("test_image.jpg"));

        // when & then
        mockMvc.perform(get("/api/v1/notices/{noticeCode}", noticeCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeCode").value(noticeCode))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images.length()").value(1))
                .andExpect(jsonPath("$.images[0].referenceType").value("NOTICE"))
                .andExpect(jsonPath("$.images[0].originName").value("test_image.jpg"));
    }

    @Test
    @Order(16)
    @DisplayName("6-3. 목록 조회 시 hasImages 필드 확인")
    @WithMockUser
    @Transactional
    void getNoticeListWithHasImagesField() throws Exception {
        // given - 이미지 포함 공지사항 생성
        Notice notice = Notice.builder()
                .noticeTitle("이미지 있음")
                .noticeContent("이미지가 있는 공지사항")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .build();
        Integer noticeCode = noticeRepository.save(notice).getNoticeCode();

        imageMapper.saveImage("NOTICE", noticeCode, createTestImageDTO("has_image.jpg"));

        // when & then
        mockMvc.perform(get("/api/v1/notices")
                        .param("page", "1")
                        .param("limit", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notices[?(@.noticeCode==" + noticeCode + ")].hasImages").value(true));
    }

    // ========== 7. 공지사항 삭제 테스트 ==========

    @Test
    @Order(17)
    @DisplayName("7-1. 공지사항 삭제 - 성공 (소프트 삭제 + 이미지 삭제)")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void deleteNotice_Success() throws Exception {
        // given - 이미지 포함 공지사항 생성
        Notice notice = Notice.builder()
                .noticeTitle("삭제 테스트")
                .noticeContent("삭제될 공지사항")
                .noticeCategory(NoticeCategory.NOTICE)
                .isImportant(false)
                .authorUserCode(testUserCode)
                .build();
        Integer noticeCode = noticeRepository.save(notice).getNoticeCode();

        imageMapper.saveImage("NOTICE", noticeCode, createTestImageDTO("to_be_deleted.jpg"));

        // 이미지 존재 확인
        List<ImageDTO> imagesBefore = imageMapper.findByReference("NOTICE", noticeCode);
        assertThat(imagesBefore).hasSize(1);

        // when - 삭제
        mockMvc.perform(delete("/api/v1/notices/{noticeCode}", noticeCode)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then - 소프트 삭제 확인
        Notice deleted = noticeRepository.findById(noticeCode).orElseThrow();
        assertThat(deleted.getNoticeStatus()).isEqualTo(NoticeStatus.DELETED);

        // 이미지 삭제 확인
        List<ImageDTO> imagesAfter = imageMapper.findByReference("NOTICE", noticeCode);
        assertThat(imagesAfter).hasSize(0);
    }

    @Test
    @Order(18)
    @DisplayName("7-2. 공지사항 삭제 - 권한 없음")
    @WithMockUser(roles = "USER")
    void deleteNotice_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/notices/{noticeCode}", testNoticeCode1)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ========== 8. 관련 공지사항 테스트 ==========

    @Test
    @Order(19)
    @DisplayName("8-1. 관련 공지사항 조회 - 같은 카테고리")
    @WithMockUser
    void getRelatedNotices_SameCategory() throws Exception {
        mockMvc.perform(get("/api/v1/notices/{noticeCode}/related", testNoticeCode1)
                        .param("limit", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].noticeCategory").value("NOTICE"));
    }

    @Test
    @Order(20)
    @DisplayName("8-2. 최신 중요 공지사항 조회")
    @WithMockUser
    void getLatestImportantNotice_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notices/latest-important"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isImportant").value(true))
                .andExpect(jsonPath("$.noticeTitle").value("중요 공지사항"))
                .andExpect(jsonPath("$.images").isArray());  // ⭐ 이미지 배열 확인
    }

    // ========== Helper 메서드 ==========

    private sisosolsol.greenfire.image.model.dto.ImageUploadDTO createTestImageDTO(String filename) {
        sisosolsol.greenfire.image.model.dto.ImageUploadDTO dto = new sisosolsol.greenfire.image.model.dto.ImageUploadDTO();
        dto.setPath("notice/20240108/" + UUID.randomUUID() + "_" + filename);
        dto.setOriginName(filename);
        dto.setFileName(UUID.randomUUID() + "_" + filename);
        return dto;
    }
}