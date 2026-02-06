package sisosolsol.greenfire.user.controller;

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
import sisosolsol.greenfire.common.config.UploadAllowConfig;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.dto.PasswordChangeRequest;
import sisosolsol.greenfire.user.dto.User;
import sisosolsol.greenfire.user.dto.UpdateUserDTO;
import sisosolsol.greenfire.user.dto.UserDTO;
import sisosolsol.greenfire.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UploadAllowConfig uploadAllowConfig;
    private final UserService userService;
    private static final UUID TEST_USER_CODE = UUID.fromString("dc31ee31-5f6f-4538-893a-462fabec8fef");

    @GetMapping("/me/summary")
    public ResponseEntity getUserSummaryData(@AuthenticationPrincipal CustomUserDetails loginUser) {
//        return ResponseEntity.ok(userService.getScrapbookSummary(loginUser.getId()));
        return ResponseEntity.ok(userService.getUserSummaryData(TEST_USER_CODE));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getUserProfile(/*@AuthenticationPrincipal CustomUserDetails loginUser*/) {
//        UserDTO userDTO = userService.getUserProfile();
        User user = userService.getUserProfile(TEST_USER_CODE);
//        UserDTO dto = UserDTO.from(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/profile-image")
    public ResponseEntity<Resource> getProfileImage(@AuthenticationPrincipal CustomUserDetails loginUser) {
        Path filePath = Paths.get(uploadAllowConfig.getDirectory(), TEST_USER_CODE.toString(), "profile.jpg");
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUserProfile(/*@AuthenticationPrincipal CustomUserDetails loginUser,*/
        @RequestPart("data") @Valid UpdateUserDTO request,
        @RequestPart(value = "image", required = false) MultipartFile file) {
//        UserDTO updatedProfile = userService.updateUserProfile(loginUser, request);
        User updatedProfile = userService.updateUserProfile(TEST_USER_CODE, request, file);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        userService.changePassword(TEST_USER_CODE, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUser(TEST_USER_CODE);
        return ResponseEntity.ok().build();
    }
}