package ru.yandex.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.yandex.practicum.constants.DateTimeConstants;
import ru.yandex.practicum.enums.RequestStatus;

import java.time.LocalDateTime;

public record ParticipationRequestDto(

        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime created,

        @JsonProperty("event")
        Long eventId,

        @JsonProperty("requester")
        Long requesterId,

        RequestStatus status
) {
}
