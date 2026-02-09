package sisosolsol.greenfire.post.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    @GetMapping("/{postCode}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Integer postCode) {
        PostDTO post = postService.getPost(postCode);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/challenge/{challengeCode}")
    public ResponseEntity<List<SimplePostDTO>> getChallengePostList(@PathVariable Integer challengeCode) {
        List<SimplePostDTO> postList = postService.getChallengePostList(challengeCode);
        return ResponseEntity.ok(postList);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge")
    public ResponseEntity<Void> createChallengePost(@RequestBody PostCreateDTO post,
                                                    Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        int postCode = postService.registChallengePost(post, currentUser.userId());
        return ResponseEntity.created(URI.create("post/" + postCode)).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{postCode}")
    public ResponseEntity<PostUpdateDTO> updatePost(@PathVariable Integer postCode,
                                                    @RequestBody PostUpdateDTO post,
                                                    Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        postService.updatePost(postCode, currentUser, post);
        return ResponseEntity.ok(post);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postCode}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postCode,
                                           Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        postService.deletePost(postCode, currentUser);
        return ResponseEntity.noContent().build();
    }
}
