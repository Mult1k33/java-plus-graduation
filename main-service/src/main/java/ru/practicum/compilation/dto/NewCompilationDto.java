package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record NewCompilationDto(
        Set<Long> events,

        boolean pinned,

        @NotBlank(message = "Заголовок подборки не может быть пустым")
        @Size(min = 1, max = 50, message = "Заголовок должен содержать от {min} до {max} символов")
        String title
) {
}
