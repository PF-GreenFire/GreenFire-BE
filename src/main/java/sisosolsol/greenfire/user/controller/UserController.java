package sisosolsol.greenfire.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.model.dto.UserDTO;
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
}
