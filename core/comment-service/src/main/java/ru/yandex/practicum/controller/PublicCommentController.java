package ru.yandex.practicum.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.service.CommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getPublishedCommentsByEvent(@PathVariable Long eventId,
                                                        @RequestParam(defaultValue = "0") @Min(0) int from,
                                                        @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get published comments: eventId={}, from={}, size={}", eventId, from, size);
        return commentService.getPublishedCommentsByEvent(eventId, from, size);
    }
}
