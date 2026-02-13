package ru.yandex.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.constants.DateTimeConstants;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.enums.EventState;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {

    Long id;

    String annotation;

    CategoryDto category;

    Long confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
    LocalDateTime createdOn;

    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    UserShortDto initiator;

    LocationDto location;

    Boolean paid;

    Integer participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
    LocalDateTime publishedOn;

    Boolean requestModeration;

    EventState state;

    String title;

    Double rating;
}
