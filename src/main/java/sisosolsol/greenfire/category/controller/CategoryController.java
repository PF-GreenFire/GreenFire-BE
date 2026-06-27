package sisosolsol.greenfire.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.category.model.dto.CategoryCreateDTO;
import sisosolsol.greenfire.category.model.dto.CategoryDTO;
import sisosolsol.greenfire.category.model.dto.CategoryUpdateDTO;
import sisosolsol.greenfire.common.enums.category.CategoryType;
import sisosolsol.greenfire.category.service.CategoryService;

import java.net.URI;
import java.util.List;

@Tag(name = "카테고리", description = "챌린지/장소 카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회")
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategoryList(@RequestParam CategoryType categoryType) {
        List<CategoryDTO> categoryList = categoryService.getCategoryList(categoryType);
        return ResponseEntity.ok(categoryList);
    }

    @Operation(summary = "카테고리 등록 (관리자)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> addCategory(@RequestBody CategoryCreateDTO category) {
        int categoryCode = categoryService.registCategory(category);
        return ResponseEntity.created(URI.create("/category/" + categoryCode)).build();
    }

    @Operation(summary = "카테고리 수정 (관리자)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{categoryCode}")
    public ResponseEntity<CategoryUpdateDTO> modifyCategory(@PathVariable Integer categoryCode,
                                               @RequestBody CategoryUpdateDTO category) {

        categoryService.updateCategory(categoryCode, category);
        return ResponseEntity.ok().body(category);
    }

    @Operation(summary = "카테고리 삭제 (관리자)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{categoryCode}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer categoryCode,
                                               @RequestBody CategoryType categoryType) {
        categoryService.deleteCategory(categoryCode, categoryType);
        return ResponseEntity.noContent().build();
    }
}
