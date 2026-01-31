package ru.yandex.practicum.compilation.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.event.mapper.EventMapper;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toEntity(NewCompilationDto request);

    @Mapping(target = "events", qualifiedByName = "toEventShortWithoutStats")
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompilationFromRequest(UpdateCompilationRequest request, @MappingTarget Compilation compilation);
}
