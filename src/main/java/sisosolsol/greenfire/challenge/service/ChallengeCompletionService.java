package sisosolsol.greenfire.challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.challenge.completion.CertificationLog;
import sisosolsol.greenfire.challenge.completion.ChallengePeriod;
import sisosolsol.greenfire.challenge.completion.CompletionPolicy;
import sisosolsol.greenfire.challenge.completion.CompletionResult;
import sisosolsol.greenfire.challenge.completion.CompletionRule;
import sisosolsol.greenfire.challenge.completion.CompletionRuleType;
import sisosolsol.greenfire.challenge.model.dao.ChallengeCompletionMapper;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.completion.CertificationDayRow;
import sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleRequest;
import sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleRow;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 완주 조건의 저장·복원·평가. 판정 로직 자체는 {@link CompletionPolicy}와 각 {@link CompletionRule}에 있고,
 * 이 서비스는 DB 행 ↔ 도메인 객체 변환과 인증 기록 조회만 맡는다.
 */
@Service
@RequiredArgsConstructor
public class ChallengeCompletionService {

    private final ChallengeCompletionMapper completionMapper;

    /** 사용자 요청 → 검증된 정책. 요청이 비어 있으면 기본 정책 */
    public CompletionPolicy buildPolicy(List<CompletionRuleRequest> requests, ChallengePeriod period) {
        if (requests == null || requests.isEmpty()) return CompletionPolicy.defaultPolicy();
        List<CompletionRule> rules = new ArrayList<>();
        for (CompletionRuleRequest request : requests) {
            if (request == null || request.getRuleType() == null) {
                throw new CustomException(ExceptionCode.COMPLETION_RULE_TYPE_REQUIRED);
            }
            rules.add(request.getRuleType().create(request.getTargetCount(), period));
        }
        return CompletionPolicy.of(rules);
    }

    /** 저장된 정책을 새 기간에 맞춰 다시 검증 (기간을 줄였을 때 연속 10일 같은 조건이 불가능해지는 경우) */
    public CompletionPolicy revalidate(CompletionPolicy policy, ChallengePeriod period) {
        List<CompletionRule> rules = new ArrayList<>();
        for (CompletionRule rule : policy.rules()) {
            rules.add(rule.type().create(rule.targetCount(), period));
        }
        return CompletionPolicy.of(rules);
    }

    @Transactional
    public void savePolicy(Integer challengeCode, CompletionPolicy policy) {
        completionMapper.deleteRules(challengeCode);
        List<CompletionRuleRow> rows = new ArrayList<>();
        int order = 0;
        for (CompletionRule rule : policy.rules()) {
            rows.add(new CompletionRuleRow(rule.type().name(), rule.targetCount(), order++));
        }
        completionMapper.insertRules(challengeCode, rows);
    }

    /** 저장된 조건이 없는 챌린지(기능 도입 전 생성분)는 기본 정책 */
    @Transactional(readOnly = true)
    public CompletionPolicy loadPolicy(Integer challengeCode) {
        List<CompletionRuleRow> rows = completionMapper.selectRules(challengeCode);
        List<CompletionRule> rules = new ArrayList<>();
        for (CompletionRuleRow row : rows) {
            rules.add(CompletionRuleType.valueOf(row.getRuleType()).restore(row.getTargetCount()));
        }
        return CompletionPolicy.of(rules);
    }

    /** 한 사용자의 현재 진행 상황 */
    @Transactional(readOnly = true)
    public CompletionResult evaluate(ChallengeDTO challenge, UUID userCode) {
        ChallengePeriod period = periodOf(challenge);
        List<LocalDate> days = completionMapper
                .selectCertificationDays(challenge.getChallengeCode(), userCode).stream()
                .map(CertificationDayRow::getCertDate)
                .toList();
        return loadPolicy(challenge.getChallengeCode()).evaluate(new CertificationLog(period, days));
    }

    /** 종료된 챌린지의 참여자 전원 평가. 인증을 한 번도 안 한 참여자도 결과에 포함된다 */
    @Transactional(readOnly = true)
    public Map<UUID, CompletionResult> evaluateAll(ChallengeDTO challenge, List<UUID> participants) {
        ChallengePeriod period = periodOf(challenge);
        CompletionPolicy policy = loadPolicy(challenge.getChallengeCode());

        Map<UUID, List<LocalDate>> daysByUser = new HashMap<>();
        for (CertificationDayRow row : completionMapper.selectCertificationDays(challenge.getChallengeCode(), null)) {
            daysByUser.computeIfAbsent(row.getUserCode(), k -> new ArrayList<>()).add(row.getCertDate());
        }

        Map<UUID, CompletionResult> results = new LinkedHashMap<>();
        for (UUID participant : participants) {
            CertificationLog log = new CertificationLog(period, daysByUser.getOrDefault(participant, List.of()));
            results.put(participant, policy.evaluate(log));
        }
        return results;
    }

    public void recordCompletion(Integer challengeCode, UUID userCode, boolean completed) {
        completionMapper.updatePartCompleted(challengeCode, userCode, completed);
    }

    public List<ChallengeDTO> findChallengesToEvaluate() {
        return completionMapper.selectChallengesToEvaluate();
    }

    public void markEvaluated(Integer challengeCode) {
        completionMapper.markEvaluated(challengeCode);
    }

    public static ChallengePeriod periodOf(ChallengeDTO challenge) {
        return new ChallengePeriod(challenge.getStartDate(), challenge.getEndDate());
    }
}
