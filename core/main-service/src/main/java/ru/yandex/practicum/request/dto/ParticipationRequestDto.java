package ru.yandex.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

import static ru.yandex.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record ParticipationRequestDto(
        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime created,

        Long event,

        Long requester,

        RequestStatus status
) {
}
