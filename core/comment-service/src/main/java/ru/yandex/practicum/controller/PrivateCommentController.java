package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.comment.*;
import ru.yandex.practicum.service.CommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @RequestParam Long eventId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Create comment: userId={}, eventId={}", userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentByUser(@PathVariable Long userId,
                                          @PathVariable Long commentId,
                                          @RequestBody @Valid UpdateCommentDto updateCommentDto) {
        log.info("Update comment: userId={}, commentId={}", userId, commentId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable Long userId,
                                    @PathVariable Long commentId) {
        log.info("Delete comment: userId={}, commentId={}", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") @Min(0) int from,
                                            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get user comments: userId={}, from={}, size={}", userId, from, size);
        return commentService.getUserComments(userId, from, size);
    }
}
