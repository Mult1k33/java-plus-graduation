package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateCompilationRequest(
        Set<Long> events,

        Boolean pinned,

        @Size(min = 1, max = 50, message = "Заголовок должен содержать от {min} до {max} символов")
        String title
) {
}
