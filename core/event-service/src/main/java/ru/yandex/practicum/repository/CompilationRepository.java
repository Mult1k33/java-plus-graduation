package ru.yandex.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT DISTINCT c FROM Compilation c LEFT JOIN FETCH c.events WHERE c.id = :compId")
    Optional<Compilation> findByIdWithEvents(@Param("compId") Long compId);

    boolean existsByTitle(String title);

    @Query("SELECT c.id FROM Compilation c WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Long> findIdsByPinned(@Param("pinned") Boolean pinned, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Compilation c LEFT JOIN FETCH c.events WHERE c.id IN :ids")
    List<Compilation> findAllByIdInWithEvents(@Param("ids") List<Long> ids);
}
