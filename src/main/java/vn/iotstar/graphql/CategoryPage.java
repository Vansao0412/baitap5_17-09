package vn.iotstar.graphql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.iotstar.entity.Category;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryPage {
    private List<Category> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
