package ru.yandex.practicum.dto.event;

import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record SearchEventAdminRequest(
        List<Long> users,

        List<EventState> states,

        List<Long> categories,

        @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime rangeEnd,

        Integer from,

        Integer size
) {
}
