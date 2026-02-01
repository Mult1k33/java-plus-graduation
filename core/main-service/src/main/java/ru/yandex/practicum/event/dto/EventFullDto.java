package ru.yandex.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.yandex.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record EventFullDto(
        Long id,

        String annotation,

        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime createdOn,

        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime eventDate,

        UserShortDto initiator,

        LocationDto location,

        Boolean paid,

        Integer participantLimit,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime publishedOn,

        Boolean requestModeration,

        EventState state,

        String title,

        Long views
) {
}
