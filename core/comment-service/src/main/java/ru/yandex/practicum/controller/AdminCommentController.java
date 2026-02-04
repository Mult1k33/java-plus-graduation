package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.comment.CommentAdminDto;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.enums.CommentStatus;
import ru.yandex.practicum.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> adminSearch(
            @RequestParam(required = false) CommentStatus status,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        log.info("Admin search: status={}, eventId={}, userId={}, start={}, end={}, from={}, size={}",
                status, eventId, userId, start, end, from, size);

        return commentService.adminSearch(status, eventId, userId, start, end, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentByAdmin(@PathVariable Long commentId,
                                           @RequestBody @Valid CommentAdminDto commentAdminDto) {
        log.info("Admin update comment: id={}, data={}", commentId, commentAdminDto);
        return commentService.updateCommentByAdmin(commentId, commentAdminDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Admin delete comment: id={}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getCommentsByEventAndStatus(@PathVariable Long eventId,
                                                        @RequestParam CommentStatus status) {
        log.info("Get comments by event and status: eventId={}, status={}", eventId, status);
        return commentService.getCommentByStatus(eventId, status);
    }
}
