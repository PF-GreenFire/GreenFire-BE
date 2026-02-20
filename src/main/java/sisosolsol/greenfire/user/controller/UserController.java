package sisosolsol.greenfire.user.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UploadAllowConfig uploadAllowConfig;
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me/summary")
    public ResponseEntity getUserSummaryData(@AuthenticationPrincipal AuthUser loginUser) {
        return ResponseEntity.ok(userService.getUserSummaryData(loginUser.userId()));
    }

    @GetMapping("/scraps/challenges")
    public ResponseEntity getScrapChallenges(@AuthenticationPrincipal AuthUser loginUser) {
        return ResponseEntity.ok(userService.getScrapChallenges(loginUser.userId()));
    }

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

    @GetMapping("/scraps/friends")
    public ResponseEntity getScrapFriends(@AuthenticationPrincipal AuthUser loginUser) {
        return ResponseEntity.ok(userService.getScrapFriends(loginUser.userId()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal AuthUser loginUser) {
        User user = userService.getUserProfile(loginUser.userId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/{userCode}/profile-image")
    public ResponseEntity<Resource> getProfileImage(@PathVariable("userCode") String userCode) {
        Path filePath = Paths.get(uploadAllowConfig.getDirectory(), "users/" + userCode, "profile.jpg");
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    @GetMapping("/me/{userCode}/cover-image")
    public ResponseEntity<Resource> getCoverImage(@PathVariable("userCode") String userCode) {
        Path filePath = Paths.get(uploadAllowConfig.getDirectory(), "users/" + userCode, "cover.jpg");
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUserProfile(@AuthenticationPrincipal AuthUser loginUser,
        @RequestPart("data") @Valid UpdateUserDTO request,
        @RequestPart(value = "image", required = false) MultipartFile file) {
        User updatedProfile = userService.updateUserProfile(loginUser.userId(), request, file);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthUser loginUser,
        @RequestBody @Valid PasswordChangeRequest request) {
        userService.changePassword(loginUser.userId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AuthUser loginUser,
        @Valid @RequestBody DeleteAccountRequest request,
        HttpServletRequest httpRequest) {
        authService.deleteAccount(loginUser.userId(), request.password(), request.reason(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/follows/{targetCode}")
    public ResponseEntity<Void> followUser(@AuthenticationPrincipal AuthUser loginUser,
        @PathVariable("targetCode") String targetUser) {
        userService.followUser(loginUser.userId(), UUID.fromString(targetUser));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/follows/{targetCode}")
    public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal AuthUser loginUser,
        @PathVariable("targetCode") String targetUser) {
        userService.deleteFollow(loginUser.userId(), UUID.fromString(targetUser));
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/me/cover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> changeCoverImage(@AuthenticationPrincipal AuthUser loginUser,
        @RequestPart("data") @Valid UpdateCoverImageDTO request,
        @RequestPart(value = "image", required = false) MultipartFile file) {
        String coverStorageKey = userService.changeCoverImage(loginUser.userId(), request, file);
        return ResponseEntity.ok(coverStorageKey);
    }

}
