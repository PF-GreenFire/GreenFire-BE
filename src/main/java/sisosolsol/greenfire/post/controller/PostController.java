package sisosolsol.greenfire.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.post.model.dto.PostCreateDTO;
import sisosolsol.greenfire.post.model.dto.PostDTO;
import sisosolsol.greenfire.post.model.dto.PostUpdateDTO;
import sisosolsol.greenfire.post.model.dto.SimplePostDTO;
import sisosolsol.greenfire.post.service.PostService;

import java.net.URI;
import java.util.List;

@Tag(name = "게시글", description = "챌린지 인증 게시글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{postCode}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Integer postCode) {
        PostDTO post = postService.getPost(postCode);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "챌린지 인증 게시글 목록 조회")
    @GetMapping("/challenge/{challengeCode}")
    public ResponseEntity<List<SimplePostDTO>> getChallengePostList(@PathVariable Integer challengeCode) {
        List<SimplePostDTO> postList = postService.getChallengePostList(challengeCode);
        return ResponseEntity.ok(postList);
    }

    @Operation(summary = "챌린지 인증 게시글 등록")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge")
    public ResponseEntity<Void> createChallengePost(@RequestBody PostCreateDTO post,
                                                    Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        int postCode = postService.registChallengePost(post, currentUser.userId());
        return ResponseEntity.created(URI.create("post/" + postCode)).build();
    }

    @Operation(summary = "게시글 수정")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{postCode}")
    public ResponseEntity<PostUpdateDTO> updatePost(@PathVariable Integer postCode,
                                                    @RequestBody PostUpdateDTO post,
                                                    Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        postService.updatePost(postCode, currentUser, post);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시글 삭제")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postCode}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postCode,
                                           Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        postService.deletePost(postCode, currentUser);
        return ResponseEntity.noContent().build();
    }
}
