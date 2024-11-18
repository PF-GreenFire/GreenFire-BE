package sisosolsol.greenfire.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchDTO;
import sisosolsol.greenfire.challenge.service.ChallengeService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<Void> createChallenge(@RequestBody ChallengeCreateDTO challenge) {
        Integer challengeCode = challengeService.registChallenge(challenge);
        return ResponseEntity.created(URI.create("/api/v1/challenges/" + challengeCode)).build();
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

    @GetMapping("/헐상세조회가이슈에없었어")
    public ResponseEntity<List<ChallengeDTO>> getChallengeListByCategory(
            @RequestBody ChallengeDTO challengeDTO
    ) {
        // 다음 이터레이션 때 구현할게요!!
        return null;
    }

    @PostMapping("/{challengeCode}/apply")
    public ResponseEntity<Void> applyChallenge(
            @PathVariable Integer challengeCode,
            @RequestBody ChallengeDTO challengeDTO) {

        return ResponseEntity.created(URI.create("/api/v1/challenges/" + challengeCode)).build();
    }
}
