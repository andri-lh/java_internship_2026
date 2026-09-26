package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.CategoryResponse;
import al.lhind.eventbooking.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories", description = "Browse event categories and manage them as an admin.")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryCatalogController {

    private final CategoryService categoryService;

    public CategoryCatalogController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "List categories for event filtering")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }
}
