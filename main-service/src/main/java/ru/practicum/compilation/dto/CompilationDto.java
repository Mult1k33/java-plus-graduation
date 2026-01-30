package ru.practicum.compilation.dto;

import ru.practicum.event.dto.EventShortDto;

import java.util.Set;

public record CompilationDto(
        Long id,
        Set<EventShortDto> events,
        boolean pinned,
        String title
) {
}
