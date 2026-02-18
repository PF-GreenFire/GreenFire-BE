package sisosolsol.greenfire.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchDTO;
import sisosolsol.greenfire.challenge.service.ChallengeService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<Void> createChallenge(@RequestBody ChallengeCreateDTO challenge) {
        Integer challengeCode = challengeService.registChallenge(challenge);
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

    @GetMapping("/{challengeCode}")
    public ResponseEntity<ChallengeDTO> getChallengeListDetails(
            @PathVariable Integer challengeCode
    ) {

        ChallengeDTO result = challengeService.getChallengeDetails(challengeCode);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{challengeCode}/apply")
    public ResponseEntity<Void> applyChallenge(
            @PathVariable Integer challengeCode
            // 유저 검사 들어갈 예정
            ) {

        challengeService.applyChallenge(challengeCode);
        return ResponseEntity.created(URI.create("/api/challenges/" + challengeCode)).build();
    }

    @DeleteMapping("/{challengeCode}/apply/cancel")
    public ResponseEntity<Void> cancelChallengePart(@PathVariable Integer challengeCode) {
        challengeService.cancelChallengePart(challengeCode);
        return ResponseEntity.created(URI.create("/api/challenges/" + challengeCode)).build();
    }
}
