package sisosolsol.greenfire.challenge.completion;

/**
 * 조건 하나의 달성 정도. current/target 단위는 조건마다 다르다 (일 수, 주 수 등).
 */
public record RuleProgress(int current, int target) {

    public boolean satisfied() {
        return current >= target;
    }
}
