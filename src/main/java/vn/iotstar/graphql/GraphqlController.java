package vn.iotstar.graphql;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;

import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphqlController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @QueryMapping
    public List<Product> products() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "unitPrice"));
    }

    @QueryMapping
    public List<Product> productsByCategory(@Argument Long categoryId) {
        requireCategory(categoryId);
        return productRepository.findByCategoryCategoryIdOrderByUnitPriceAsc(categoryId);
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "categoryId"));
    }

    @QueryMapping
    public Category category(@Argument Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public ProductPage productsPage(@Argument Integer page, @Argument Integer size,
                                    @Argument String keyword) {
        Pageable pageable = pageRequest(page, size, "unitPrice");
        Page<Product> result = blank(keyword)
                ? productRepository.findAll(pageable)
                : productRepository.findByProductNameContainingIgnoreCase(keyword.trim(), pageable);
        return new ProductPage(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @QueryMapping
    public CategoryPage categoriesPage(@Argument Integer page, @Argument Integer size,
                                       @Argument String keyword) {
        Pageable pageable = pageRequest(page, size, "categoryId");
        Page<Category> result = blank(keyword)
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByCategoryNameContainingIgnoreCase(keyword.trim(), pageable);
        return new CategoryPage(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @MutationMapping
    public Category createCategory(@Argument CategoryInput input) {
        validateCategoryInput(input);
        if (categoryRepository.findByCategoryNameIgnoreCase(input.categoryName().trim()).isPresent()) {
            throw new IllegalArgumentException("Category đã tồn tại trong hệ thống");
        }
        Category category = new Category();
        category.setCategoryName(input.categoryName().trim());
        category.setIcon(blank(input.icon()) ? null : input.icon().trim());
        return categoryRepository.save(category);
    }

    @MutationMapping
    public Category updateCategory(@Argument Long id, @Argument CategoryInput input) {
        validateCategoryInput(input);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy category"));
        category.setCategoryName(input.categoryName().trim());
        if (!blank(input.icon())) category.setIcon(input.icon().trim());
        return categoryRepository.save(category);
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument Long id) {
        if (!categoryRepository.existsById(id)) return false;
        categoryRepository.deleteById(id);
        return true;
    }

    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        validateProductInput(input);
        if (productRepository.findByProductNameIgnoreCase(input.productName().trim()).isPresent()) {
            throw new IllegalArgumentException("Sản phẩm đã tồn tại trong hệ thống");
        }
        Product product = new Product();
        applyProductInput(product, input);
        product.setCreateDate(new Date());
        return productRepository.save(product);
    }

    @MutationMapping
    public Product updateProduct(@Argument Long id, @Argument ProductInput input) {
        validateProductInput(input);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy product"));
        applyProductInput(product, input);
        return productRepository.save(product);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument Long id) {
        if (!productRepository.existsById(id)) return false;
        productRepository.deleteById(id);
        return true;
    }

    private void applyProductInput(Product product, ProductInput input) {
        Category category = requireCategory(input.categoryId());
        product.setProductName(input.productName().trim());
        product.setUnitPrice(input.unitPrice());
        product.setDiscount(input.discount() == null ? 0 : input.discount());
        product.setQuantity(input.quantity());
        product.setStatus((short) input.status().intValue());
        product.setDescription(input.description());
        if (!blank(input.images())) product.setImages(input.images().trim());
        product.setCategory(category);
    }

    private Category requireCategory(Long categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Category không được để trống");
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category không tồn tại"));
    }

    private static Pageable pageRequest(Integer page, Integer size, String sort) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size < 1 || size > 50 ? 5 : size;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, sort));
    }

    private static void validateCategoryInput(CategoryInput input) {
        if (input == null || blank(input.categoryName())) {
            throw new IllegalArgumentException("Tên category không được để trống");
        }
    }

    private static void validateProductInput(ProductInput input) {
        if (input == null || blank(input.productName()) || input.unitPrice() == null
                || input.quantity() == null || input.status() == null) {
            throw new IllegalArgumentException("Thiếu thông tin product bắt buộc");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
