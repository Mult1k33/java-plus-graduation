package ru.practicum.event.dto;

import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record SearchEventAdminRequest(
        List<Long> users,

        List<EventState> states,

        List<Long> categories,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeEnd,

        Integer from,

        Integer size
) {
}
