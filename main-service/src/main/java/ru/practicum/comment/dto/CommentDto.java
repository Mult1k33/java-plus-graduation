package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import static ru.practicum.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record CommentDto(
        Long id,

        String text,

        Long userId,

        Long eventId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        LocalDateTime createdDate
) {
}
