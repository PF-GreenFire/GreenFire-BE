package sisosolsol.greenfire.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.model.dto.UserListResponseDTO;
import sisosolsol.greenfire.user.service.UserManagementService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/users")
    public ResponseEntity<List<UserListResponseDTO>> getAllUsers(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }
}
