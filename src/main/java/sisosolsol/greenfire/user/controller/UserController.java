package sisosolsol.greenfire.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.user.model.dto.UserDTO;
import sisosolsol.greenfire.user.model.dto.UserUpdateDTO;
import sisosolsol.greenfire.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/profiles/me")
    public ResponseEntity<UserDTO> getUserProfile(Authentication authentication) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        UserDTO userDTO = userService.getUserProfile(currentUser.userId());
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/profiles/me")
    public ResponseEntity<UserDTO> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDTO request) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        UserDTO updatedProfile = userService.updateUserProfile(currentUser.userId(), request);
        return ResponseEntity.ok(updatedProfile);
    }
}