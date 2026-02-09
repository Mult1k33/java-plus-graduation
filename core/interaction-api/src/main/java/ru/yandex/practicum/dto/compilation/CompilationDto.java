package ru.yandex.practicum.dto.compilation;

import ru.yandex.practicum.dto.event.EventShortDto;

import java.util.Set;

public record CompilationDto(

        Long id,

        Set<EventShortDto> events,

        boolean pinned,

        String title
) {
}
