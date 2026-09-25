package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.CategoryRequest;
import al.lhind.eventbooking.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long categoryId, CategoryRequest request);
    CategoryResponse getById(Long categoryId);
    List<CategoryResponse> getAll();
    void delete(Long categoryId);
}