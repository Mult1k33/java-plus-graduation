package ru.yandex.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.enums.CommentStatus;

public record CommentAdminDto(
        @NotBlank(message = "Комментарий не может быть пустым")
        @Size(min = 1, max = 5000, message = "Комментарий должен содержать от {min} до {max} символов")
        String text,

        @NotNull
        CommentStatus status
) {
}
