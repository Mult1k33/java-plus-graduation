package ru.yandex.practicum.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.constants.DateTimeConstants;
import ru.yandex.practicum.enums.CommentStatus;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,

        String text,

        Long userId,

        Long eventId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime createdDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN)
        LocalDateTime updatedDate,

        CommentStatus status
) {
}
