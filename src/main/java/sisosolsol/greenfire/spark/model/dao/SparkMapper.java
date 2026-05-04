package sisosolsol.greenfire.spark.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface SparkMapper {

    /** users.total_spark에 amount 가산하고 갱신된 누적값을 반환 */
    int addSpark(@Param("userCode") UUID userCode,
                 @Param("amount") int amount);

    int getTotalSpark(@Param("userCode") UUID userCode);

    void insertHistory(@Param("userCode") UUID userCode,
                       @Param("action") String action,
                       @Param("amount") int amount,
                       @Param("sourceType") String sourceType,
                       @Param("sourceCode") Integer sourceCode);

    /** 특정 user/action/source 조합의 이력 카운트 (어뷰즈 방어용) */
    int countHistory(@Param("userCode") UUID userCode,
                     @Param("action") String action,
                     @Param("sourceType") String sourceType,
                     @Param("sourceCode") Integer sourceCode);
}
