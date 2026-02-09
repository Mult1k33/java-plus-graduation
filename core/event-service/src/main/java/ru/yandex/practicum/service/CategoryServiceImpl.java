package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.category.*;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.CategoryMapper;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.repository.CategoryRepository;
import ru.yandex.practicum.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByNameIgnoreCaseAndTrim(newCategoryDto.name())) {
            throw new ConflictException("Категория с таким названием уже существует");
        }

        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        log.info("Создана категория: {}", savedCategory);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryByIdOrThrow(categoryId);

        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Невозможно удалить категорию с существующими событиями");
        }

        categoryRepository.delete(category);
        log.info("Удалена категория с ID: {}", categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, UpdateCategoryDto updateCategoryDto) {
        Category category = getCategoryByIdOrThrow(categoryId);

        if (categoryRepository.findByNameAndIdNot(updateCategoryDto.name(), categoryId).isPresent()) {
            throw new ConflictException("Категория с таким названием уже существует");
        }

        categoryMapper.updateCategoryFromDto(updateCategoryDto, category);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Обновлена категория {}", updatedCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        List<Category> categoriesPage = categoryRepository.findAllList(pageable);

        if (categoriesPage.isEmpty()) {
            return List.of();
        }

        List<CategoryDto> result = categoriesPage.stream()
                .map(categoryMapper::toDto)
                .toList();

        log.debug("Найдено {} категорий", result.size());
        return result;
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        Category category = getCategoryByIdOrThrow(categoryId);
        return categoryMapper.toDto(category);
    }


    private Category getCategoryByIdOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
    }
}
