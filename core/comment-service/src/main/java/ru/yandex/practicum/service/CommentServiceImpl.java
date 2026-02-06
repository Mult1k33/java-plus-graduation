package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.EventClient;
import ru.yandex.practicum.client.UserClient;
import ru.yandex.practicum.dto.comment.*;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.enums.CommentStatus;
import ru.yandex.practicum.enums.EventState;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.CommentMapper;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventClient eventClient;
    private final UserClient userClient;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        checkUserExists(userId);
        EventFullDto event = eventClient.getEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя комментировать неопубликованное событие");
        }

        Comment comment = commentMapper.toComment(newCommentDto, userId, eventId);
        Comment saved = commentRepository.save(comment);

        log.info("Создан комментарий: id={}, userId={}, eventId={}", saved.getId(), userId, eventId);
        return commentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = getCommentOrThrow(commentId, userId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Можно редактировать только комментарии в статусе PENDING");
        }

        commentMapper.updateCommentFromDto(updateCommentDto, comment);
        comment.setUpdatedDate(LocalDateTime.now());

        log.info("Комментарий обновлен пользователем: commentId={}, userId={}", commentId, userId);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId, userId);

        comment.setStatus(CommentStatus.REJECTED);
        comment.setUpdatedDate(LocalDateTime.now());
        commentRepository.save(comment);

        log.info("Комментарий помечен как удаленный пользователем: commentId={}, userId={}", commentId, userId);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(Long commentId, CommentAdminDto commentAdminDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        if (commentAdminDto.status() == CommentStatus.PENDING) {
            throw new ConflictException("Целевой статус модерации не может быть PENDING");
        }

        commentMapper.updateFromAdminDto(commentAdminDto, comment);
        comment.setUpdatedDate(LocalDateTime.now());

        log.info("Комментарий обновлен администратором: commentId={}", commentId);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с id=" + commentId + " не найден");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий удален администратором: commentId={}", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentByStatus(Long eventId, CommentStatus status) {
        checkEventExists(eventId);

        log.info("Получение комментариев по статусу: eventId={}, status={}", eventId, status);
        return commentRepository.findByEventIdAndStatus(eventId, status)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getPublishedCommentsByEvent(Long eventId, int from, int size) {
        checkEventExists(eventId);

        Pageable pageable = PageRequest.of(from / size, size);
        log.info("Получение опубликованных комментариев: eventId={}, from={}, size={}", eventId, from, size);

        return commentRepository.findPublishedByEvent(eventId, pageable)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> adminSearch(CommentStatus status,
                                        Long eventId,
                                        Long userId,
                                        LocalDateTime start,
                                        LocalDateTime end,
                                        int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> result;

        if (start == null && end == null) {
            result = commentRepository.searchWithoutDates(status, eventId, userId, pageable);
        } else if (start == null) {
            result = commentRepository.searchWithDates(status, eventId, userId, LocalDateTime.MIN, end, pageable);
        } else if (end == null) {
            result = commentRepository.searchWithDates(status, eventId, userId, start, LocalDateTime.MAX, pageable);
        } else {
            result = commentRepository.searchWithDates(status, eventId, userId, start, end, pageable);
        }

        log.info("Поиск администратором: status={}, eventId={}, userId={}, from={}, size={}",
                status, eventId, userId, from, size);

        return result.getContent()
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        checkUserExists(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        log.info("Получение комментариев пользователя: userId={}, from={}, size={}", userId, from, size);

        return commentRepository.findByUserId(userId, pageable)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    private Comment getCommentOrThrow(Long commentId, Long userId) {
        return commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " от пользователя с id=" + userId + " не найден"
                ));
    }

    private void checkUserExists(Long userId) {
        try {
            userClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void checkEventExists(Long eventId) {
        try {
            eventClient.getEvent(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Событие с id=" + eventId + " не найден");
        }
    }
}
