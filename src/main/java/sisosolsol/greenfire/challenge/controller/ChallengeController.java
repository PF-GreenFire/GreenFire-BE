package sisosolsol.greenfire.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeUpdateDTO;
import sisosolsol.greenfire.challenge.service.ChallengeService;
import sisosolsol.greenfire.common.security.model.AuthUser;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<Void> createChallenge(
            @RequestBody ChallengeCreateDTO challenge,
            @AuthenticationPrincipal AuthUser loginUser) {
        Integer challengeCode = challengeService.registChallenge(challenge, loginUser.userId());
        return ResponseEntity.created(URI.create("/api/challenges/" + challengeCode)).build();
    }

    @GetMapping
    public ResponseEntity<ChallengeSearchDTO> getChallengeList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Integer categoryCode) {

        ChallengeSearchDTO result = challengeService.getChallenges(page, size, searchKeyword, categoryCode);
        return ResponseEntity.ok(result);
    }

    // 마감 임박 챌린지 (RECRUITING + endDate ASC, 메인 페이지 섹션용)
    @GetMapping("/closing-soon")
    public ResponseEntity<java.util.List<ChallengeDTO>> getClosingSoon(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(challengeService.getClosingSoonChallenges(limit));
    }

    @GetMapping("/{challengeCode}")
    public ResponseEntity<ChallengeDTO> getChallengeListDetails(
            @PathVariable Integer challengeCode
    ) {

        ChallengeDTO result = challengeService.getChallengeDetails(challengeCode);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{challengeCode}")
    public ResponseEntity<Void> updateChallenge(
            @PathVariable Integer challengeCode,
            @RequestBody ChallengeUpdateDTO update,
            @AuthenticationPrincipal AuthUser loginUser) {
        challengeService.updateChallenge(challengeCode, update, loginUser.userId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{challengeCode}")
    public ResponseEntity<Void> deleteChallenge(
            @PathVariable Integer challengeCode,
            @AuthenticationPrincipal AuthUser loginUser) {
        challengeService.deleteChallenge(challengeCode, loginUser.userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{challengeCode}/apply")
    public ResponseEntity<Void> applyChallenge(
            @PathVariable Integer challengeCode,
            @AuthenticationPrincipal AuthUser loginUser) {

        challengeService.applyChallenge(challengeCode, loginUser.userId());
        return ResponseEntity.created(URI.create("/api/challenges/" + challengeCode)).build();
    }

    @DeleteMapping("/{challengeCode}/apply/cancel")
    public ResponseEntity<Void> cancelChallengePart(
            @PathVariable Integer challengeCode,
            @AuthenticationPrincipal AuthUser loginUser) {
        challengeService.cancelChallengePart(challengeCode, loginUser.userId());
        return ResponseEntity.noContent().build();
    }
}
