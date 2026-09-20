package vn.iotstar.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.Category;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IStorageService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/category")
@Tag(name = "Category API", description = "CRUD danh mục sản phẩm")
public class CategoryApiController {
    private final ICategoryService categoryService;
    private final IStorageService storageService;

    public CategoryApiController(ICategoryService categoryService, IStorageService storageService) {
        this.categoryService = categoryService;
        this.storageService = storageService;
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả category")
    public ResponseEntity<Response> getAllCategory() {
        return ResponseEntity.ok(new Response(true, "Thành công", categoryService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy một category theo id")
    public ResponseEntity<Response> getCategory(@PathVariable Long id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(value -> ResponseEntity.ok(new Response(true, "Thành công", value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response(false, "Không tìm thấy category", null)));
    }

    @PostMapping("/getCategory")
    public ResponseEntity<Response> getCategoryByGuide(@RequestParam("id") Long id) {
        return getCategory(id);
    }

    @PostMapping({"", "/addCategory"})
    @Operation(summary = "Thêm category")
    public ResponseEntity<Response> addCategory(
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        if (categoryService.findByCategoryName(categoryName).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new Response(false, "Category đã tồn tại trong hệ thống", null));
        }

        Category category = new Category();
        category.setCategoryName(categoryName.trim());
        saveIcon(category, icon);
        Category saved = categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response(true, "Thêm thành công", saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật category")
    public ResponseEntity<Response> updateCategory(
            @PathVariable Long id,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        return update(id, categoryName, icon);
    }

    @PutMapping("/updateCategory")
    public ResponseEntity<Response> updateCategoryByGuide(
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        return update(categoryId, categoryName, icon);
    }

    private ResponseEntity<Response> update(Long id, String categoryName, MultipartFile icon) {
        Optional<Category> optional = categoryService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false, "Không tìm thấy category", null));
        }

        Category category = optional.get();
        category.setCategoryName(categoryName.trim());
        saveIcon(category, icon);
        return ResponseEntity.ok(new Response(true, "Cập nhật thành công", categoryService.save(category)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa category")
    public ResponseEntity<Response> deleteCategory(@PathVariable Long id) {
        return delete(id);
    }

    @DeleteMapping("/deleteCategory")
    public ResponseEntity<Response> deleteCategoryByGuide(@RequestParam("categoryId") Long categoryId) {
        return delete(categoryId);
    }

    private ResponseEntity<Response> delete(Long id) {
        Optional<Category> optional = categoryService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false, "Không tìm thấy category", null));
        }
        categoryService.deleteById(id);
        return ResponseEntity.ok(new Response(true, "Xóa thành công", optional.get()));
    }

    private void saveIcon(Category category, MultipartFile icon) {
        if (icon != null && !icon.isEmpty()) {
            String filename = storageService.getStorageFilename(icon,
                    UUID.randomUUID().toString());
            category.setIcon(filename);
            storageService.store(icon, filename);
        }
    }
}
