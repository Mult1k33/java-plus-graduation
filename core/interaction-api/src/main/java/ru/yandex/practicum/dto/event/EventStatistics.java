package ru.yandex.practicum.dto.event;

import java.util.Map;

public record EventStatistics(
        Map<Long, Integer> confirmedRequests,
        Map<Long, Long> views
) {
}
