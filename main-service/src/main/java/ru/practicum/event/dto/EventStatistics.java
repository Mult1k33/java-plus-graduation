package ru.practicum.event.dto;

import java.util.Map;

public record EventStatistics(
        Map<Long, Integer> confirmedRequests,
        Map<Long, Long> views
) {
}
