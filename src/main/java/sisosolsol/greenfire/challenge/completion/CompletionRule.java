package sisosolsol.greenfire.challenge.completion;

/**
 * 완주 조건 하나. 구현체는 "인증 기록을 보고 얼마나 달성했는지"만 판단한다.
 *
 * <p>새 조건 유형 추가 방법:
 * <ol>
 *   <li>이 인터페이스를 구현한 클래스를 {@code completion.rule} 패키지에 추가</li>
 *   <li>{@link CompletionRuleType}에 상수 한 줄 추가 (라벨, 횟수 범위, 생성 방법)</li>
 * </ol>
 * 저장·조회·평가·API·FE 편집기는 유형 목록을 그대로 따라가므로 따로 고칠 곳이 없다.
 */
public interface CompletionRule {

    CompletionRuleType type();

    /** 사용자가 설정한 횟수. 횟수가 없는 유형(예: 매일 인증)은 null */
    Integer targetCount();

    RuleProgress evaluate(CertificationLog log);

    /** 사용자에게 보여줄 조건 설명 (예: "매주 3일 이상 인증") */
    String describe();
}
