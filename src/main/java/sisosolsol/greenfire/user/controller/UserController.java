package sisosolsol.greenfire.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.model.dto.UserDTO;
import sisosolsol.greenfire.user.model.dto.UserUpdateDTO;
import sisosolsol.greenfire.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/profiles/me")
    public ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal CustomUserDetails loginUser) {
        UserDTO userDTO = userService.getUserProfile(loginUser);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/profiles/me")
    public ResponseEntity<UserDTO> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails loginUser,
            @Valid @RequestBody UserUpdateDTO request) {
        UserDTO updatedProfile = userService.updateUserProfile(loginUser, request);
        return ResponseEntity.ok(updatedProfile);
    }
}