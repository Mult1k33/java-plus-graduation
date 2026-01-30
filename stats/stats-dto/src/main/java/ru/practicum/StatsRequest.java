package ru.practicum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record StatsRequest(
        LocalDateTime start,
        LocalDateTime end,
        List<String> uris,
        boolean unique
) {
    public static StatsRequest of(String start, String end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        return new StatsRequest(
                LocalDateTime.parse(start, formatter),
                LocalDateTime.parse(end, formatter),
                uris,
                unique
        );
    }
}
