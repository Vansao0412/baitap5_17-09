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
import vn.iotstar.entity.Product;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.IStorageService;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@Tag(name = "Product API", description = "CRUD sản phẩm")
public class ProductApiController {
    private final IProductService productService;
    private final ICategoryService categoryService;
    private final IStorageService storageService;

    public ProductApiController(IProductService productService,
                                ICategoryService categoryService,
                                IStorageService storageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.storageService = storageService;
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả product")
    public ResponseEntity<Response> getAllProduct() {
        return ResponseEntity.ok(new Response(true, "Thành công", productService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy một product theo id")
    public ResponseEntity<Response> getProduct(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(value -> ResponseEntity.ok(new Response(true, "Thành công", value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response(false, "Không tìm thấy product", null)));
    }

    @PostMapping({"", "/addProduct"})
    @Operation(summary = "Thêm product")
    public ResponseEntity<Response> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam("unitPrice") double unitPrice,
            @RequestParam("discount") double discount,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("quantity") int quantity,
            @RequestParam("status") short status) {
        if (productService.findByProductName(productName).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new Response(false, "Sản phẩm đã tồn tại trong hệ thống", null));
        }
        Optional<Category> category = categoryService.findById(categoryId);
        if (category.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new Response(false, "Category không tồn tại", null));
        }

        Product product = new Product();
        product.setProductName(productName.trim());
        product.setUnitPrice(unitPrice);
        product.setDiscount(discount);
        product.setDescription(description);
        product.setCategory(category.get());
        product.setQuantity(quantity);
        product.setStatus(status);
        product.setCreateDate(new Date());
        saveImage(product, imageFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response(true, "Thêm thành công", productService.save(product)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật product")
    public ResponseEntity<Response> updateProduct(
            @PathVariable Long id,
            @RequestParam("productName") String productName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam("unitPrice") double unitPrice,
            @RequestParam("discount") double discount,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("quantity") int quantity,
            @RequestParam("status") short status) {
        Optional<Product> optional = productService.findById(id);
        Optional<Category> category = categoryService.findById(categoryId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false, "Không tìm thấy product", null));
        }
        if (category.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new Response(false, "Category không tồn tại", null));
        }

        Product product = optional.get();
        product.setProductName(productName.trim());
        product.setUnitPrice(unitPrice);
        product.setDiscount(discount);
        product.setDescription(description);
        product.setCategory(category.get());
        product.setQuantity(quantity);
        product.setStatus(status);
        saveImage(product, imageFile);
        return ResponseEntity.ok(new Response(true, "Cập nhật thành công", productService.save(product)));
    }

    @PutMapping("/updateProduct")
    public ResponseEntity<Response> updateProductByGuide(
            @RequestParam("productId") Long productId,
            @RequestParam("productName") String productName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam("unitPrice") double unitPrice,
            @RequestParam("discount") double discount,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("quantity") int quantity,
            @RequestParam("status") short status) {
        return updateProduct(productId, productName, imageFile, unitPrice, discount,
                description, categoryId, quantity, status);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa product")
    public ResponseEntity<Response> deleteProduct(@PathVariable Long id) {
        Optional<Product> optional = productService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false, "Không tìm thấy product", null));
        }
        productService.deleteById(id);
        return ResponseEntity.ok(new Response(true, "Xóa thành công", optional.get()));
    }

    @DeleteMapping("/deleteProduct")
    public ResponseEntity<Response> deleteProductByGuide(@RequestParam("productId") Long productId) {
        return deleteProduct(productId);
    }

    private void saveImage(Product product, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = storageService.getStorageFilename(imageFile,
                    UUID.randomUUID().toString());
            product.setImages(filename);
            storageService.store(imageFile, filename);
        }
    }
}
