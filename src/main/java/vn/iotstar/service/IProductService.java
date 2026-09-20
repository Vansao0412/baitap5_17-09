package vn.iotstar.service;

import vn.iotstar.entity.Product;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Optional<Product> findByProductName(String name);
    Product save(Product product);
    void deleteById(Long id);
}
