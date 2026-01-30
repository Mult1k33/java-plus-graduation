package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentDto(
        @NotBlank(message = "Комментарий не может быть пустым")
        @Size(min = 1, max = 5000, message = "Комментарий должен содержать от {min} до {max} символов")
        String text
) {
}
