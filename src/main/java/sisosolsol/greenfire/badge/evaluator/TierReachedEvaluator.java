package sisosolsol.greenfire.badge.evaluator;

import sisosolsol.greenfire.badge.model.BadgeContext;
import sisosolsol.greenfire.common.enums.spark.Tier;

/**
 * 특정 등급에 도달하면 부여. outcomeCount는 무시하고 현재 tier만 본다.
 */
public class TierReachedEvaluator implements BadgeEvaluator {

    private final Tier required;

    public TierReachedEvaluator(Tier required) {
        this.required = required;
    }

    @Override
    public boolean evaluate(BadgeContext context) {
        Tier current = context.getTier();
        if (current == null) return false;
        return current.getLevel() >= required.getLevel();
    }
}
