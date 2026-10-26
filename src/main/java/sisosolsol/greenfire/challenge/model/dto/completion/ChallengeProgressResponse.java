package sisosolsol.greenfire.challenge.model.dto.completion;

import sisosolsol.greenfire.challenge.completion.CompletionResult;

import java.util.List;

/** 내 완주 진행 상황 */
public record ChallengeProgressResponse(boolean completed, int certifiedDays, int periodDays,
                                        List<RuleProgressResponse> rules) {

    public record RuleProgressResponse(String ruleType, String label, Integer targetCount, String description,
                                       int current, int target, boolean satisfied) {}

    public static ChallengeProgressResponse of(CompletionResult result, int periodDays) {
        List<RuleProgressResponse> rules = result.evaluations().stream()
                .map(e -> new RuleProgressResponse(
                        e.rule().type().name(), e.rule().type().getLabel(), e.rule().targetCount(),
                        e.rule().describe(), e.progress().current(), e.progress().target(),
                        e.progress().satisfied()))
                .toList();
        return new ChallengeProgressResponse(result.completed(), result.certifiedDays(), periodDays, rules);
    }
}
