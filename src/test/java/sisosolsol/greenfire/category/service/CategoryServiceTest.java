package sisosolsol.greenfire.category.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.category.model.dao.CategoryMapper;
import sisosolsol.greenfire.common.enums.category.CategoryType;
import sisosolsol.greenfire.common.exception.CustomException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("deleteCategory(CHALLENGE): 사용 중이 아니면 정상적으로 삭제한다")
    void deleteCategory_challenge_success_whenNotInUse() {
        // Arrange
        Integer categoryCode = 1;
        when(categoryMapper.countActiveChallengesByCategoryCode(categoryCode)).thenReturn(0);

        // Act
        categoryService.deleteCategory(categoryCode, CategoryType.CHALLENGE);

        // Assert
        verify(categoryMapper).deleteChallengeCategory(categoryCode);
        verify(categoryMapper, never()).deleteStoreCategory(categoryCode);
    }

    @Test
    @DisplayName("deleteCategory(CHALLENGE): 사용 중이면 CATEGORY_IN_USE 예외를 던진다")
    void deleteCategory_challenge_throwsInUse_whenActiveChallengesExist() {
        // Arrange
        Integer categoryCode = 2;
        when(categoryMapper.countActiveChallengesByCategoryCode(categoryCode)).thenReturn(3);

        // Act + Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryCode, CategoryType.CHALLENGE))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용 중");
        verify(categoryMapper, never()).deleteChallengeCategory(categoryCode);
    }

    @Test
    @DisplayName("deleteCategory(STORE): 사용 중이 아니면 정상적으로 삭제한다")
    void deleteCategory_store_success_whenNotInUse() {
        // Arrange
        Integer categoryCode = 5;
        when(categoryMapper.countActiveStoresByCategoryCode(categoryCode)).thenReturn(0);

        // Act
        categoryService.deleteCategory(categoryCode, CategoryType.STORE);

        // Assert
        verify(categoryMapper).deleteStoreCategory(categoryCode);
        verify(categoryMapper, never()).deleteChallengeCategory(categoryCode);
    }

    @Test
    @DisplayName("deleteCategory(STORE): 사용 중이면 CATEGORY_IN_USE 예외를 던진다")
    void deleteCategory_store_throwsInUse_whenActiveStoresExist() {
        // Arrange
        Integer categoryCode = 6;
        when(categoryMapper.countActiveStoresByCategoryCode(categoryCode)).thenReturn(1);

        // Act + Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryCode, CategoryType.STORE))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용 중");
        verify(categoryMapper, never()).deleteStoreCategory(categoryCode);
    }
}
