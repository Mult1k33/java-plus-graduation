package ru.yandex.practicum.event.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.event.dto.LocationDto;
import ru.yandex.practicum.event.model.EventLocation;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    EventLocation toEventLocation(LocationDto dto);

    LocationDto toDto(EventLocation entity);
}
