package vn.iotstar.graphql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.iotstar.entity.Product;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductPage {
    private List<Product> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
