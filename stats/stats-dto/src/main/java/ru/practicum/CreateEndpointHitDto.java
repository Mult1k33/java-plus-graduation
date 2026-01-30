package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record CreateEndpointHitDto(
        @NotBlank(message = "Название приложения не может быть пустым")
        String app,

        @NotBlank(message = "URI не может быть пустым")
        String uri,

        @NotBlank(message = "IP адрес не может быть пустым")
        String ip,

        @NotNull(message = "Временная метка не может быть null")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime timestamp
) {
}
