package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record NewEventDto(
        @NotBlank(message = "Краткое описание не может быть пустым")
        @Size(min = 20, max = 2000, message = "Аннотация должна содержать от {min} до {max} символов")
        String annotation,

        @NotNull(message = "Id категории не может быть null")
        Long category,

        @NotBlank(message = "Описание события не может быть пустым")
        @Size(min = 20, max = 7000, message = "Описание должно содержать от {min} до {max} символов")
        String description,

        @NotNull(message = "Дата события не может быть null")
        @Future(message = "Дата события должна быть в будущем")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime eventDate,

        @NotNull(message = "Место проведения события не может быть null")
        LocationDto location,

        Boolean paid,

        @PositiveOrZero(message = "Лимит участников события должен быть нулевым или больше нуля")
        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank(message = "Название события не может быть пустым")
        @Size(min = 3, max = 120, message = "Заголовок должен содержать от {min} до {max} символов")
        String title
) {
    public NewEventDto {
        if (paid == null) {
            paid = Boolean.FALSE;
        }
        if (participantLimit == null) {
            participantLimit = 0;
        }
        if (requestModeration == null) {
            requestModeration = Boolean.TRUE;
        }
    }
}
