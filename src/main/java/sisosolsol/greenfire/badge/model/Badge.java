package sisosolsol.greenfire.badge.model;

import lombok.Getter;
import sisosolsol.greenfire.badge.evaluator.BadgeEvaluator;
import sisosolsol.greenfire.badge.evaluator.ThresholdEvaluator;
import sisosolsol.greenfire.badge.evaluator.TierReachedEvaluator;
import sisosolsol.greenfire.common.enums.spark.Tier;

/**
 * 뱃지 정의.
 *
 * 새 뱃지 추가 = 여기 enum 한 줄.
 * 단순 누적 임계는 ThresholdEvaluator(N), 등급 도달은 TierReachedEvaluator(Tier),
 * 더 복잡한 조건은 BadgeEvaluator 구현체를 추가하고 여기서 사용.
 *
 * ⚠️ enum.name()이 DB의 user_badge.badge_code로 저장된다 — rename 절대 금지.
 */
@Getter
public enum Badge {

    // ─── 이정표 (인증글) ───
    FIRST_POST   ("첫 발걸음",     "이정표", "첫 인증글을 작성했어요",       "🌱",
                  Outcome.POST_CREATED, new ThresholdEvaluator(1)),
    POST_10      ("꾸준함의 시작", "이정표", "인증글 10개를 작성했어요",     "📔",
                  Outcome.POST_CREATED, new ThresholdEvaluator(10)),
    POST_50      ("기록의 사람",   "이정표", "인증글 50개를 작성했어요",     "📚",
                  Outcome.POST_CREATED, new ThresholdEvaluator(50)),
    POST_100     ("백 번의 약속",  "이정표", "인증글 100개를 작성했어요",    "💯",
                  Outcome.POST_CREATED, new ThresholdEvaluator(100)),

    // ─── 이정표 (매장) ───
    STORE_FIRST  ("초록불 발견자", "이정표", "첫 매장 제보가 승인됐어요",    "📍",
                  Outcome.STORE_APPROVED, new ThresholdEvaluator(1)),
    STORE_5      ("동네 안내인",   "이정표", "매장 5개가 승인됐어요",        "🗺",
                  Outcome.STORE_APPROVED, new ThresholdEvaluator(5)),

    // ─── 이정표 (챌린지 완료) ───
    CHALLENGE_FIRST("첫 도전 완료", "이정표", "첫 챌린지를 완주했어요",      "🏁",
                    Outcome.CHALLENGE_COMPLETE, new ThresholdEvaluator(1)),
    CHALLENGE_5    ("도전의 연속", "이정표", "챌린지 5개를 완주했어요",      "🎯",
                    Outcome.CHALLENGE_COMPLETE, new ThresholdEvaluator(5)),

    // ─── 등급 도달 ───
    TIER_SPROUT  ("새싹의 보호자", "등급",   "달팽이 등급에 도달했어요",     "🐌",
                  Outcome.TIER_REACHED, new TierReachedEvaluator(Tier.SNAIL)),
    TIER_OTTER   ("수달의 보호자", "등급",   "수달 등급에 도달했어요",        "🦦",
                  Outcome.TIER_REACHED, new TierReachedEvaluator(Tier.OTTER)),
    TIER_TIGER   ("호랑이의 보호자","등급",  "호랑이 등급에 도달했어요",      "🐯",
                  Outcome.TIER_REACHED, new TierReachedEvaluator(Tier.TIGER));

    private final String label;
    private final String category;
    private final String description;
    private final String emoji;
    private final Outcome outcome;
    private final BadgeEvaluator evaluator;

    Badge(String label, String category, String description, String emoji,
          Outcome outcome, BadgeEvaluator evaluator) {
        this.label = label;
        this.category = category;
        this.description = description;
        this.emoji = emoji;
        this.outcome = outcome;
        this.evaluator = evaluator;
    }
}
