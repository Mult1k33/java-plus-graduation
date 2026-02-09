package ru.yandex.practicum.dto.event;

import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.constants.DateTimeConstants;
import ru.yandex.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

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
