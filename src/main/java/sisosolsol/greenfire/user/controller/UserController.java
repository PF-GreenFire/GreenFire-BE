package sisosolsol.greenfire.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.auth.dto.DeleteAccountRequest;
import sisosolsol.greenfire.auth.service.AuthService;
import sisosolsol.greenfire.common.config.UploadAllowConfig;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.user.dto.PasswordChangeRequest;
import sisosolsol.greenfire.user.dto.UpdateCoverImageDTO;
import sisosolsol.greenfire.user.dto.User;
import sisosolsol.greenfire.user.dto.UpdateUserDTO;
import sisosolsol.greenfire.user.service.UserService;

@Tag(name = "사용자", description = "사용자 프로필, 스크랩, 팔로우 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UploadAllowConfig uploadAllowConfig;
    private final UserService userService;
    private final AuthService authService;

    /** 다른 사용자 공개 프로필. viewer 비로그인 OK (isFollowing=false) */
    @Operation(summary = "다른 사용자 공개 프로필 조회")
    @GetMapping("/profile/{userId}")
    public ResponseEntity<sisosolsol.greenfire.user.dto.PublicProfileResponse> getPublicProfile(
            @PathVariable("userId") String userId,
            @AuthenticationPrincipal AuthUser viewer) {
        java.util.UUID targetCode;
        try {
            targetCode = java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        java.util.UUID viewerCode = viewer != null ? viewer.userId() : null;
        sisosolsol.greenfire.user.dto.PublicProfileResponse profile =
                userService.getPublicProfile(targetCode, viewerCode);
        if (profile == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "내 요약 정보 조회")
    @GetMapping("/me/summary")
    public ResponseEntity getUserSummaryData(@AuthenticationPrincipal AuthUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.getUserSummaryData(loginUser.userId()));
    }

    @Operation(summary = "내 스크랩 챌린지 목록 조회")
    @GetMapping("/scraps/challenges")
    public ResponseEntity getScrapChallenges(@AuthenticationPrincipal AuthUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.getScrapChallenges(loginUser.userId()));
    }

    @Operation(summary = "스크랩 챌린지 썸네일 조회")
    @GetMapping("/scraps/challenges/{challengeCode}/thumbnail")
    public ResponseEntity<Resource> getChallengeThumbnail(@PathVariable("challengeCode") String challengeCode) {
        Path filePath = Paths.get(uploadAllowConfig.getDirectory(), "challenges/" + challengeCode, "thumbnail.jpg");
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    @Operation(summary = "내 스크랩 친구 목록 조회")
    @GetMapping("/scraps/friends")
    public ResponseEntity getScrapFriends(@AuthenticationPrincipal AuthUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.getScrapFriends(loginUser.userId()));
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal AuthUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        User user = userService.getUserProfile(loginUser.userId());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "내 프로필 이미지 조회")
    @GetMapping("/me/profile-image/{profileImageCode}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable("profileImageCode") int profileImageCode) {
        Path filePath = userService.getProfileImage(profileImageCode);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    @Operation(summary = "내 커버 이미지 조회")
    @GetMapping("/me/cover-image/{coverImageCode}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable("coverImageCode") int coverImageCode) {
        Path filePath = userService.getCoverImage(coverImageCode);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    @Operation(summary = "내 프로필 수정 (멀티파트: 데이터 + 이미지)")
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUserProfile(@AuthenticationPrincipal AuthUser loginUser,
        @RequestPart("data") @Valid UpdateUserDTO request,
        @RequestPart(value = "image", required = false) MultipartFile file) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        User updatedProfile = userService.updateUserProfile(loginUser.userId(), request, file);
        return ResponseEntity.ok(updatedProfile);
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthUser loginUser,
        @RequestBody @Valid PasswordChangeRequest request) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        userService.changePassword(loginUser.userId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AuthUser loginUser,
        @Valid @RequestBody DeleteAccountRequest request,
        HttpServletRequest httpRequest) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        authService.deleteAccount(loginUser.userId(), request.password(), request.reason(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 팔로우")
    @PostMapping("/follows/{targetCode}")
    public ResponseEntity<Void> followUser(@AuthenticationPrincipal AuthUser loginUser,
        @PathVariable("targetCode") String targetUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        userService.followUser(loginUser.userId(), UUID.fromString(targetUser));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 언팔로우")
    @DeleteMapping("/follows/{targetCode}")
    public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal AuthUser loginUser,
        @PathVariable("targetCode") String targetUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        userService.deleteFollow(loginUser.userId(), UUID.fromString(targetUser));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 커버 이미지 변경 (멀티파트)")
    @PutMapping(value = "/me/cover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> changeCoverImage(@AuthenticationPrincipal AuthUser loginUser,
        @RequestPart("data") @Valid UpdateCoverImageDTO request,
        @RequestPart(value = "image", required = false) MultipartFile file) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        String coverStorageKey = userService.changeCoverImage(loginUser.userId(), request, file);
        return ResponseEntity.ok(coverStorageKey);
    }

}
