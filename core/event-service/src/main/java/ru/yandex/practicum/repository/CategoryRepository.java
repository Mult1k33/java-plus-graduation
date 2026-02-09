package ru.yandex.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(@Param("name") String name);

    Optional<Category> findByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT c FROM Category c")
    List<Category> findAllList(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END" +
            " FROM Category c WHERE LOWER(TRIM(c.name)) = LOWER(TRIM(:name))")
    boolean existsByNameIgnoreCaseAndTrim(@Param("name") String name);
}
