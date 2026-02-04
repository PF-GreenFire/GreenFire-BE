package sisosolsol.greenfire.user.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.dto.UserDTO;
import sisosolsol.greenfire.user.dto.UserUpdateDTO;
import sisosolsol.greenfire.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final UUID TEST_USER_CODE = UUID.fromString("dc31ee31-5f6f-4538-893a-462fabec8fef");

    @GetMapping("/me/summary")
    public ResponseEntity getUserSummaryData(@AuthenticationPrincipal CustomUserDetails loginUser) {
//        return ResponseEntity.ok(userService.getScrapbookSummary(loginUser.getId()));
        return ResponseEntity.ok(userService.getUserSummaryData(TEST_USER_CODE));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUserProfile(/*@AuthenticationPrincipal CustomUserDetails loginUser*/) {
//        UserDTO userDTO = userService.getUserProfile();
        UserDTO userDTO = userService.getUserProfile(TEST_USER_CODE);
        return ResponseEntity.ok(userDTO);
    }


    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> updateUserProfile(/*@AuthenticationPrincipal CustomUserDetails loginUser,*/
        @RequestPart("data") @Valid UserUpdateDTO request,
        @RequestPart(value = "image", required = false) MultipartFile file) {
//        UserDTO updatedProfile = userService.updateUserProfile(loginUser, request);
        UserDTO updatedProfile = userService.updateUserProfile(TEST_USER_CODE, request);
        return ResponseEntity.ok(updatedProfile);
    }
}