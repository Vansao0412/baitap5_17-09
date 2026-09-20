package vn.iotstar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import vn.iotstar.config.StorageProperties;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.IStorageService;

import java.util.Date;
import java.util.Optional;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class SpringbootThymeleafApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootThymeleafApplication.class, args);
    }

    @Bean
    CommandLineRunner init(IStorageService storageService,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        return args -> {
            storageService.init();

            Category phone = findOrCreateCategory(categoryRepository, "Điện thoại", "phone.png");
            Category laptop = findOrCreateCategory(categoryRepository, "Laptop", "laptop.png");
            Category accessories = findOrCreateCategory(categoryRepository, "Phụ kiện", "headphones.png");

            Product phoneProduct = productRepository.findByProductNameIgnoreCase("Điện thoại Nova X1")
                    .orElseGet(() -> productRepository.findByProductNameIgnoreCase("Sản phẩm mẫu")
                            .orElseGet(Product::new));
            phoneProduct.setProductName("Điện thoại Nova X1");
            phoneProduct.setQuantity(18);
            phoneProduct.setUnitPrice(7990000);
            phoneProduct.setDiscount(5);
            phoneProduct.setDescription("Điện thoại màn hình đẹp, pin tốt, phù hợp sử dụng hằng ngày.");
            phoneProduct.setStatus((short) 1);
            phoneProduct.setImages("phone.png");
            phoneProduct.setCategory(phone);
            if (phoneProduct.getCreateDate() == null) phoneProduct.setCreateDate(new Date());
            productRepository.save(phoneProduct);

            saveSampleProduct(productRepository, "Laptop Air 14", 8, 15990000, 10,
                    "Laptop mỏng nhẹ cho học tập và làm việc.", "laptop.png", laptop);
            saveSampleProduct(productRepository, "Tai nghe không dây Pro", 25, 1290000, 0,
                    "Tai nghe không dây âm thanh rõ, đeo êm và tiện lợi.", "headphones.png", accessories);
        };
    }

    private static Category findOrCreateCategory(CategoryRepository repository,
                                                 String name, String icon) {
        Category category = repository.findByCategoryNameIgnoreCase(name).orElseGet(Category::new);
        category.setCategoryName(name);
        if (category.getIcon() == null) category.setIcon(icon);
        return repository.save(category);
    }

    private static void saveSampleProduct(ProductRepository repository, String name, int quantity,
                                          double price, double discount, String description,
                                          String image, Category category) {
        Optional<Product> optional = repository.findByProductNameIgnoreCase(name);
        Product product = optional.orElseGet(Product::new);
        product.setProductName(name);
        product.setQuantity(quantity);
        product.setUnitPrice(price);
        product.setDiscount(discount);
        product.setDescription(description);
        product.setImages(image);
        product.setStatus((short) 1);
        product.setCategory(category);
        if (product.getCreateDate() == null) product.setCreateDate(new Date());
        repository.save(product);
    }
}
