package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record ParticipationRequestDto(
        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime created,

        Long event,

        Long requester,

        RequestStatus status
) {
}
