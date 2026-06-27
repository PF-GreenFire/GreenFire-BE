package sisosolsol.greenfire.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.feed.model.dto.CommentDTO;
import sisosolsol.greenfire.feed.model.dto.CommentRequest;
import sisosolsol.greenfire.feed.model.dto.FeedCreateRequest;
import sisosolsol.greenfire.feed.model.dto.FeedDetailDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListItemDTO;
import sisosolsol.greenfire.feed.model.dto.FeedListResponse;
import sisosolsol.greenfire.feed.model.dto.LikeToggleResponse;
import sisosolsol.greenfire.feed.service.FeedService;

import java.net.URI;

import java.util.List;

@Tag(name = "피드", description = "피드 등록/조회, 댓글, 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    // 피드 목록 (cursor 기반 무한스크롤)
    @Operation(summary = "피드 목록 조회 (cursor 기반 무한스크롤)")
    @GetMapping
    public ResponseEntity<FeedListResponse> getFeedList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer cursorPostCode,
            @RequestParam(required = false) Double cursorScore,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthUser user) {
        // type/cursorScore는 추천 알고리즘 도입 후 활용 (지금은 최신순 cursor)
        return ResponseEntity.ok(
                feedService.getFeedList(user != null ? user.userId() : null, cursorPostCode, size)
        );
    }

    // 피드 등록 (multipart: data + images)
    @Operation(summary = "피드 등록 (멀티파트: 데이터 + 이미지)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createFeedPost(
            @RequestPart("data") FeedCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Integer postCode = feedService.createPost(request, images, user.userId());
        return ResponseEntity.created(URI.create("/api/feed/" + postCode)).build();
    }

    // 추천 피드
    @Operation(summary = "추천 피드 조회")
    @GetMapping("/featured")
    public ResponseEntity<List<FeedListItemDTO>> getFeatured(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(
                feedService.getFeaturedPosts(user != null ? user.userId() : null, limit)
        );
    }

    // 피드 상세 조회 (좋아요 여부 포함을 위해 인증 정보 사용)
    @Operation(summary = "피드 상세 조회")
    @GetMapping("/{postCode}")
    public ResponseEntity<FeedDetailDTO> getFeedDetail(@PathVariable Integer postCode,
                                                       @AuthenticationPrincipal AuthUser user) {
        FeedDetailDTO detail = feedService.getFeedDetail(postCode, user != null ? user.userId() : null);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 좋아요 토글
    @Operation(summary = "피드 좋아요 토글")
    @PostMapping("/{postCode}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(@PathVariable Integer postCode,
                                                         @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(feedService.toggleLike(postCode, user.userId()));
    }

    // 댓글 목록
    @Operation(summary = "피드 댓글 목록 조회")
    @GetMapping("/{postCode}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Integer postCode) {
        return ResponseEntity.ok(feedService.getComments(postCode));
    }

    // 댓글 등록
    @Operation(summary = "피드 댓글 등록")
    @PostMapping("/{postCode}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Integer postCode,
                                                 @RequestBody CommentRequest request,
                                                 @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CommentDTO created = feedService.addComment(postCode, user.userId(), request.getContent().trim());
        return ResponseEntity.ok(created);
    }

    // 댓글 삭제 (본인 댓글만)
    @Operation(summary = "피드 댓글 삭제 (본인 댓글만)")
    @DeleteMapping("/comments/{commentCode}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentCode,
                                              @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        feedService.deleteComment(commentCode, user.userId());
        return ResponseEntity.ok().build();
    }
}
