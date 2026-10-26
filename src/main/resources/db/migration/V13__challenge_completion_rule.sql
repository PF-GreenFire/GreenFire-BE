-- 챌린지 완주 조건 (챌린지 생성자가 설정, 모두 충족해야 완주)
-- rule_type: TOTAL_DAYS / EVERY_DAY / STREAK  (CompletionRuleType enum 이름)
-- target_count: 횟수가 없는 유형(EVERY_DAY)은 NULL
CREATE TABLE IF NOT EXISTS challenge_completion_rule (
    rule_code      SERIAL PRIMARY KEY,
    challenge_code INTEGER NOT NULL REFERENCES challenge(challenge_code) ON DELETE CASCADE,
    rule_type      VARCHAR(30) NOT NULL,
    target_count   INTEGER,
    rule_order     INTEGER NOT NULL DEFAULT 0,
    UNIQUE (challenge_code, rule_type)
);

-- 참여자별 완주 여부 (NULL = 아직 평가 전)
ALTER TABLE challenge_part ADD COLUMN IF NOT EXISTS completed BOOLEAN;

-- 종료 후 완주 평가를 마친 시각 (스케줄러가 한 챌린지를 한 번만 평가하도록)
ALTER TABLE challenge ADD COLUMN IF NOT EXISTS completion_evaluated_at TIMESTAMP WITH TIME ZONE;

-- 이 마이그레이션 이전에 이미 종료된 챌린지는 기존 방식으로 보상이 끝났으므로 재평가하지 않는다
UPDATE challenge
SET completion_evaluated_at = CURRENT_TIMESTAMP
WHERE challenge_status = 'CLOSED'
  AND completion_evaluated_at IS NULL;
