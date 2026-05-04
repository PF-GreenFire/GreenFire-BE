package sisosolsol.greenfire.spark.model.dto;

import sisosolsol.greenfire.common.enums.spark.Tier;

/**
 * 사용자의 보상 정보 — 누적 spark + 현재 등급 + 다음 등급까지 남은 spark.
 * /api/auth/me 응답 등에 포함.
 */
public record SparkInfo(
        int total,
        String tierCode,    // BEE, SNAIL, ...
        String tierLabel,   // 꿀벌, 달팽이, ...
        String tierEmoji,
        int tierLevel,      // 1~9
        Integer nextThreshold,  // 다음 등급의 threshold (정상이면 null)
        int nextThresholdGap    // 다음 등급까지 필요한 spark (정상이면 0)
) {
    public static SparkInfo from(int totalSpark) {
        Tier tier = Tier.of(totalSpark);
        Tier next = tier.next();
        Integer nextThreshold = (next == tier) ? null : next.getThreshold();
        return new SparkInfo(
                totalSpark,
                tier.name(),
                tier.getLabel(),
                tier.getEmoji(),
                tier.getLevel(),
                nextThreshold,
                tier.nextThresholdGap(totalSpark)
        );
    }
}
