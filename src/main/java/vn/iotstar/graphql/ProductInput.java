package vn.iotstar.graphql;

public record ProductInput(String productName, Double unitPrice, Double discount,
                           Integer quantity, Integer status, String description,
                           String images, Long categoryId) {
}
