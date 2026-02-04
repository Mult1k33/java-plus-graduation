package ru.yandex.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.constants.DateTimeConstants;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;


public record EventFullDto(
        Long id,

        String annotation,

        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime createdOn,

        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime eventDate,

        UserShortDto initiator,

        LocationDto location,

        Boolean paid,

        Integer participantLimit,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime publishedOn,

        Boolean requestModeration,

        EventState state,

        String title,

        Long views
) {
}
