package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.event.LocationDto;
import ru.yandex.practicum.model.EventLocation;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    EventLocation toEventLocation(LocationDto dto);

    LocationDto toDto(EventLocation entity);
}
