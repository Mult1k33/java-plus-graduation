package ru.yandex.practicum.compilation.dto;

import ru.yandex.practicum.event.dto.EventShortDto;

import java.util.Set;

public record CompilationDto(
        Long id,
        Set<EventShortDto> events,
        boolean pinned,
        String title
) {
}
