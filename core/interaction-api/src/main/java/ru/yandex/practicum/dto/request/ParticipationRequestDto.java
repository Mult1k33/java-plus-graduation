package ru.yandex.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.constants.DateTimeConstants;
import ru.yandex.practicum.enums.RequestStatus;

import java.time.LocalDateTime;

public record ParticipationRequestDto(
        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime created,

        Long event,

        Long requester,

        RequestStatus status
) {
}
