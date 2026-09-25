package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.request.CategoryRequest;
import al.lhind.eventbooking.dto.response.CategoryResponse;
import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.service.CategoryService;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw duplicateName();
        }

        Category category = new Category();
        category.setName(name);

        try {
            return toResponse(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists", exception);
        }
    }

    @Override
    @Transactional
    public CategoryResponse update(Long categoryId, CategoryRequest request) {
        Category category = requireCategory(categoryId);
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(
                name, categoryId)) {
            throw duplicateName();
        }

        category.setName(name);

        try {
            return toResponse(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long categoryId) {
        return toResponse(requireCategory(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = requireCategory(categoryId);

        if (eventRepository.existsByCategories_Id(categoryId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category cannot be removed because it is used by events");
        }

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category cannot be removed because it is in use",
                    exception);
        }
    }

    private Category requireCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));
    }

    private ResponseStatusException duplicateName() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT, "Category name already exists");
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}