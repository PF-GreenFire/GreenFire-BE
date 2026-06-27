package sisosolsol.greenfire.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "챌린지", description = "챌린지 등록, 조회, 참여, 취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @Operation(summary = "챌린지 등록")
    @PostMapping
    public ResponseEntity<Void> createChallenge(
            @RequestBody ChallengeCreateDTO challenge,
            @AuthenticationPrincipal AuthUser loginUser) {
        Integer challengeCode = challengeService.registChallenge(challenge, loginUser.userId());
        return ResponseEntity.created(URI.create("/api/challenges/" + challengeCode)).build();
    }

    @Operation(summary = "챌린지 목록 조회 (검색/카테고리/페이징)")
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
    @Operation(summary = "마감 임박 챌린지 조회 (메인 페이지용)")
    @GetMapping("/closing-soon")
    public ResponseEntity<java.util.List<ChallengeDTO>> getClosingSoon(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(challengeService.getClosingSoonChallenges(limit));
    }

    @Operation(summary = "챌린지 상세 조회")
    @GetMapping("/{challengeCode}")
    public ResponseEntity<ChallengeDTO> getChallengeListDetails(
            @PathVariable Integer challengeCode
    ) {

        ChallengeDTO result = challengeService.getChallengeDetails(challengeCode);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "챌린지 수정")
    @PatchMapping("/{challengeCode}")
    public ResponseEntity<Void> updateChallenge(
            @PathVariable Integer challengeCode,
            @RequestBody ChallengeUpdateDTO update,
            @AuthenticationPrincipal AuthUser loginUser) {
        challengeService.updateChallenge(challengeCode, update, loginUser.userId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "챌린지 삭제")
    @DeleteMapping("/{challengeCode}")
    public ResponseEntity<Void> deleteChallenge(
            @PathVariable Integer challengeCode,
            @AuthenticationPrincipal AuthUser loginUser) {
        challengeService.deleteChallenge(challengeCode, loginUser.userId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "챌린지 참여 신청")
    @PostMapping("/{challengeCode}/apply")
    public ResponseEntity<Void> applyChallenge(
            @PathVariable Integer challengeCode,
            @AuthenticationPrincipal AuthUser loginUser) {

        challengeService.applyChallenge(challengeCode, loginUser.userId());
        return ResponseEntity.created(URI.create("/api/challenges/" + challengeCode)).build();
    }

    @Operation(summary = "챌린지 참여 취소")
    @DeleteMapping("/{challengeCode}/apply/cancel")
    public ResponseEntity<Void> cancelChallengePart(
            @PathVariable Integer challengeCode,
            @AuthenticationPrincipal AuthUser loginUser) {
        challengeService.cancelChallengePart(challengeCode, loginUser.userId());
        return ResponseEntity.noContent().build();
    }
}
