package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.comment.CommentAdminDto;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.dto.comment.NewCommentDto;
import ru.yandex.practicum.dto.comment.UpdateCommentDto;
import ru.yandex.practicum.enums.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteComment(Long userId, Long commentId);

    CommentDto updateCommentByAdmin(Long commentId, CommentAdminDto commentAdminDto);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getCommentByStatus(Long eventId, CommentStatus status);

    List<CommentDto> getPublishedCommentsByEvent(Long eventId, int from, int size);

    List<CommentDto> adminSearch(CommentStatus status,
                                 Long eventId,
                                 Long userId,
                                 LocalDateTime start,
                                 LocalDateTime end,
                                 int from,
                                 int size);

    public List<CommentDto> getUserComments(Long userId, int from, int size);
}
