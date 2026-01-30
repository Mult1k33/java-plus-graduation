package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {
        if (compilationRepository.existsByTitle(request.title()))
            throw new ConflictException("Подборка с таким названием (" + request.title() + ") уже существует");

        Compilation compilation = compilationMapper.toEntity(request);

        if (request.events() != null && !request.events().isEmpty()) {
            List<Event> events = eventRepository.findAllByEventIds(new ArrayList<>(request.events()));

            if (events.size() != request.events().size()) throw new NotFoundException("Не все события найдены");
            compilation.setEvents(new HashSet<>(events));

        } else compilation.setEvents(new HashSet<>());
        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Создана подборка: {}", request);
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("Подборка с идентификатором " + compId + " не найдена");

        log.info("Удалена подборка с id={}", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationOrThrow(compId);

        if (request.title() != null
                && !request.title().equals(compilation.getTitle())
                && compilationRepository.existsByTitle(request.title())) {
            throw new ConflictException("Подборка с названием \"" + request.title() + "\" уже существует");
        }

        if (request.events() != null) {
            if (request.events().isEmpty()) {
                compilation.setEvents(new HashSet<>());
            } else {
                List<Event> events = eventRepository.findAllByEventIds(new ArrayList<>(request.events()));
                if (events.size() != request.events().size()) {
                    throw new NotFoundException("Некоторые события не найдены");
                }
                compilation.setEvents(new HashSet<>(events));
            }
        }

        compilationMapper.updateCompilationFromRequest(request, compilation);

        log.info("Обновлена подборка с id={}", compId);
        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("id").ascending()
        );

        List<Long> ids = compilationRepository.findIdsByPinned(pinned, sortedPageable);

        if (ids == null || ids.isEmpty()) {
            log.info("Список подборок пуст pinned={}, pageable={}", pinned, pageable);
            return List.of();
        }

        List<Compilation> compilationsWithEvents = compilationRepository.findAllByIdInWithEvents(ids);

        Map<Long, Compilation> byId = compilationsWithEvents.stream()
                .collect(Collectors.toMap(Compilation::getId, Function.identity()));

        List<Compilation> ordered = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        log.info("Получен список подборок pinned={}, pageable={}", pinned, pageable);
        return ordered.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilationOrThrow(compId);

        log.info("Получена подборка с id={}", compId);
        return compilationMapper.toDto(compilation);
    }

    private Compilation getCompilationOrThrow(Long compId) {
        return compilationRepository.findByIdWithEvents(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с идентификатором " + compId + " не найдена"));
    }
}
