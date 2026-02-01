package ru.yandex.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    void updateCategoryFromDto(NewCategoryDto dto, @MappingTarget Category category);

    Category toEntityFromDto(CategoryDto dto);
}
