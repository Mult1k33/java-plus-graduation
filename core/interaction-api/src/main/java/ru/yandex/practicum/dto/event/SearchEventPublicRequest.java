package ru.yandex.practicum.dto.event;

import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.event.model.SortState;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record SearchEventPublicRequest(
        String text,

        List<Long> categories,

        Boolean paid,

        @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime rangeEnd,

        Boolean onlyAvailable,

        SortState sort,

        Integer from,

        Integer size
) {
    public SearchEventPublicRequest {
        if (onlyAvailable == null) {
            onlyAvailable = false;
        }
        if (sort == null) {
            sort = SortState.EVENT_DATE;
        }
        if (from == null) {
            from = 0;
        }
        if (size == null) {
            size = 10;
        }
    }
}
