package sisosolsol.greenfire.category.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.category.model.dto.CategoryCreateDTO;
import sisosolsol.greenfire.category.model.dto.CategoryDTO;
import sisosolsol.greenfire.category.model.dto.CategoryUpdateDTO;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<CategoryDTO> getChallengeCategoryList();

    List<CategoryDTO> getStoreCategoryList();

    void registChallengeCategory(CategoryCreateDTO category);

    void registStoreCategory(CategoryCreateDTO category);

    int updateChallengeCategory(@Param("categoryCode") Integer categoryCode, @Param("category") CategoryUpdateDTO category);

    int updateStoreCategory(@Param("categoryCode") Integer categoryCode, @Param("category") CategoryUpdateDTO category);

    int deleteChallengeCategory(Integer categoryCode);

    int deleteStoreCategory(Integer categoryCode);

    int countActiveChallengesByCategoryCode(Integer categoryCode);

    int countActiveStoresByCategoryCode(Integer categoryCode);
}
