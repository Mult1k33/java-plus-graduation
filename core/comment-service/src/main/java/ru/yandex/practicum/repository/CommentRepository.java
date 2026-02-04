package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.enums.CommentStatus;
import ru.yandex.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Поиск опубликованных комментов события
    @Query("""
            SELECT c FROM Comment c
            WHERE c.eventId = :eventId AND c.status = ru.yandex.practicum.enums.CommentStatus.PUBLISHED
            ORDER BY c.createdDate DESC
            """)
    List<Comment> findPublishedByEvent(@Param("eventId") Long eventId, Pageable pageable);

    // Поиск комментария по его ID и ID автора
    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    // Расширенный поиск комментариев по фильтрам (без фильтрации по датам) для модерации
    @Query("""
            SELECT c FROM Comment c
            WHERE (:status IS NULL OR c.status = :status)
            AND (:eventId IS NULL OR c.eventId = :eventId)
            AND (:userId IS NULL OR c.userId = :userId)
            ORDER BY c.createdDate DESC
            """)
    Page<Comment> searchWithoutDates(@Param("status") CommentStatus status,
                                     @Param("eventId") Long eventId,
                                     @Param("userId") Long userId,
                                     Pageable pageable);

    // Расширенный поиск комментариев по фильтрам (с фильтрацией по датам) для модерации
    @Query("""
            SELECT c FROM Comment c
            WHERE (:status IS NULL OR c.status = :status)
            AND (:eventId IS NULL OR c.eventId = :eventId)
            AND (:userId IS NULL OR c.userId = :userId)
            AND (c.createdDate >= :start)
            AND (c.createdDate <= :end)
            ORDER BY c.createdDate DESC
            """)
    Page<Comment> searchWithDates(@Param("status") CommentStatus status,
                                  @Param("eventId") Long eventId,
                                  @Param("userId") Long userId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    // Поиск комментариев к событию по статусу
    @Query("""
            SELECT c FROM Comment c
            WHERE c.eventId = :eventId AND c.status = :status
            ORDER BY c.createdDate DESC
            """)
    List<Comment> findByEventIdAndStatus(@Param("eventId") Long eventId,
                                         @Param("status") CommentStatus status);

    // Поиск всех комментариев к событию
    List<Comment> findByEventId(Long eventId, Pageable pageable);

    // Поиск всех комментариев пользователя
    List<Comment> findByUserId(Long userId, Pageable pageable);
}
