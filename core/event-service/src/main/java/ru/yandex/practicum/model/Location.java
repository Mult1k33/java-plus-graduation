package ru.yandex.practicum.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @NotNull(message = "Широта не может быть null")
    private Float lat;

    @NotNull(message = "Долгота не может быть null")
    private Float lon;
}
