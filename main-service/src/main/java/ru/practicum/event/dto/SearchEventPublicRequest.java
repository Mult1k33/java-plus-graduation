package ru.practicum.event.dto;

import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.event.model.SortState;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record SearchEventPublicRequest(
        String text,

        List<Long> categories,

        Boolean paid,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
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
