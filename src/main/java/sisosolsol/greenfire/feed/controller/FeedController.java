package sisosolsol.greenfire.feed.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.feed.model.dto.CommentDTO;
import sisosolsol.greenfire.feed.model.dto.CommentRequest;
import sisosolsol.greenfire.feed.model.dto.FeedDetailDTO;
import sisosolsol.greenfire.feed.model.dto.LikeToggleResponse;
import sisosolsol.greenfire.feed.service.FeedService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    // 피드 상세 조회 (좋아요 여부 포함을 위해 인증 정보 사용)
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
    @PostMapping("/{postCode}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(@PathVariable Integer postCode,
                                                         @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(feedService.toggleLike(postCode, user.userId()));
    }

    // 댓글 목록
    @GetMapping("/{postCode}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Integer postCode) {
        return ResponseEntity.ok(feedService.getComments(postCode));
    }

    // 댓글 등록
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
