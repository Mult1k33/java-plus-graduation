package ru.practicum;

public record ViewStatsDto(
        String app,
        String uri,
        Long hits
) {
}
