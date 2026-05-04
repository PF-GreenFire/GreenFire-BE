package sisosolsol.greenfire.common.enums.spark;

import lombok.Getter;

@Getter
public enum Tier {

    BEE(1, "꿀벌", "🐝", 0),
    SNAIL(2, "달팽이", "🐌", 100),
    FIREFLY(3, "반딧불이", "✨", 300),
    OTTER(4, "수달", "🦦", 700),
    CRANE(5, "두루미", "🕊", 1500),
    WOLF(6, "늑대", "🐺", 3000),
    TIGER(7, "호랑이", "🐯", 6000),
    EMPEROR_PENGUIN(8, "황제펭귄", "🐧", 10000),
    CORAL_REEF(9, "산호초", "🪸", 15000);

    private final int level;
    private final String label;
    private final String emoji;
    private final int threshold;

    Tier(int level, String label, String emoji, int threshold) {
        this.level = level;
        this.label = label;
        this.emoji = emoji;
        this.threshold = threshold;
    }

    /** 누적 spark 값으로 현재 등급 판정 */
    public static Tier of(int totalSpark) {
        Tier current = BEE;
        for (Tier t : values()) {
            if (totalSpark >= t.threshold) current = t;
        }
        return current;
    }

    /** 다음 등급 (정상이면 자기 자신) */
    public Tier next() {
        Tier[] all = values();
        if (this.level >= all.length) return this;
        return all[this.level]; // level은 1-based, 배열은 0-based이므로 다음 인덱스가 level
    }

    /** 다음 등급까지 필요한 spark (정상이면 0) */
    public int nextThresholdGap(int totalSpark) {
        Tier next = next();
        if (next == this) return 0;
        return Math.max(0, next.threshold - totalSpark);
    }
}
