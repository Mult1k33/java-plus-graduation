package ru.yandex.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.event.model.StateActionAdmin;

import java.time.LocalDateTime;

import static ru.yandex.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record UpdateEventAdminRequest(
        @Size(min = 20, max = 2000, message = "Недопустимое количество символов")
        String annotation,

        Long category,

        @Size(min = 20, max = 7000, message = "Описание должно содержать от {min} до {max} символов")
        String description,

        @Future(message = "Дата события должна быть в будущем")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime eventDate,

        LocationDto location,

        Boolean paid,

        @PositiveOrZero(message = "Лимит участников события должен быть нулевым или больше нуля")
        Integer participantLimit,

        Boolean requestModeration,

        StateActionAdmin stateAction,

        @Size(min = 3, max = 120, message = "Заголовок должен содержать от {min} до {max} символов")
        String title
) {
}
