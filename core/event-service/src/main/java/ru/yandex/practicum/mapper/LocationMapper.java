package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.event.LocationDto;
import ru.yandex.practicum.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toLocation(LocationDto dto);

    LocationDto toDto(Location entity);
}
