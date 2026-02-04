package ru.yandex.practicum.dto.event;

import jakarta.validation.constraints.NotNull;

public record LocationDto(
       @NotNull(message = "Широта не может быть null")
       Float lat,

       @NotNull(message = "Долгота не может быть null")
       Float lon
) {
}
