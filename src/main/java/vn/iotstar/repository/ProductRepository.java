package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductNameIgnoreCase(String productName);
    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
    List<Product> findByCategoryCategoryIdOrderByUnitPriceAsc(Long categoryId);
}
