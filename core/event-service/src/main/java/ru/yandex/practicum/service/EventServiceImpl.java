package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.CreateEndpointHitDto;
import ru.yandex.practicum.StatsClientApp;
import ru.yandex.practicum.ViewStatsDto;
import ru.yandex.practicum.client.RequestClient;
import ru.yandex.practicum.client.UserClient;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.enums.*;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.EventMapper;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.repository.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final StatsClientApp statsClient;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateDateEvent(newEventDto.eventDate(), 2);

        UserShortDto userDto = userClient.getUser(userId);

        Category category = categoryRepository.findById(newEventDto.category())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.category() + " не найдена"));

        Event event = eventMapper.toEntity(newEventDto, userId, category);
        event = eventRepository.save(event);

        log.info("Создано событие с id={}, title={}, initiatorId={}", event.getId(), event.getTitle(), userId);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setInitiator(userDto);
        dto.setConfirmedRequests(0L);
        dto.setViews(0L);
        return dto;
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        userClient.getUser(userId);

        List<Event> events = eventRepository.findAllByInitiatorIdOrderByCreatedOnDesc(userId, pageable);

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        EventStatistics stats = getEventStatistics(eventIds);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests((long) stats.confirmedRequests()
                            .getOrDefault(event.getId(), 0L));
                    dto.setViews(stats.views().getOrDefault(event.getId(), 0L));

                    UserShortDto initiatorDto = userClient.getUser(event.getInitiatorId());
                    dto.setInitiator(initiatorDto);
                    return dto;
                })
                .toList();
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId, String ip) {
        userClient.getUser(userId);

        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Событие c id " + eventId + " не найдено у пользователя с id " + userId);
        }

        saveHit("/events/" + eventId, ip);

        return buildFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userClient.getUser(userId);

        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId))
            throw new ValidationException("Пользователь не является инициатором события и не может его редактировать");

        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationException("Изменять можно только не опубликованные события");

        validateDateEvent(request.eventDate(), 2);

        Category category = null;
        if (request.category() != null) {
            category = categoryRepository.findById(request.category())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.category() + " не найдена"));
        }
        eventMapper.updateEventFromUserRequest(request, event, category);

        if (request.stateAction() != null) {
            if (request.stateAction() == StateActionUser.SEND_TO_REVIEW)
                event.pending();
            if (request.stateAction() == StateActionUser.CANCEL_REVIEW)
                event.canceled();
        }

        Event updatedEvent = eventRepository.save(event);
        return buildFullDto(updatedEvent);
    }

    @Override
    public Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));
    }

    @Override
    public List<EventFullDto> getEventsAdmin(SearchEventAdminRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.rangeStart(), request.rangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.users() != null && !request.users().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereUsers(request.users()));
        if (request.states() != null && !request.states().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereStates(request.states()));
        if (request.categories() != null && !request.categories().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.categories()));
        if (request.rangeStart() != null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(request.rangeStart()));
        if (request.rangeEnd() != null)
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.rangeEnd()));
        if (request.rangeStart() == null && request.rangeEnd() == null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(LocalDateTime.now()));

        Page<Event> eventsPage = eventRepository.findAll(specification, pageable);
        List<Long> eventIds = eventsPage.getContent().stream()
                .map(Event::getId)
                .toList();

        if (eventIds.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllByEventIds(eventIds);

        if (events.isEmpty()) return List.of();

        List<Long> searchEventIds = events.stream()
                .map(Event::getId)
                .toList();

        EventStatistics stats = getEventStatistics(searchEventIds);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    dto.setConfirmedRequests((long) stats.confirmedRequests()
                            .getOrDefault(event.getId(), 0L));
                    dto.setViews(stats.views().getOrDefault(event.getId(), 0L));

                    UserShortDto initiatorDto = userClient.getUser(event.getInitiatorId());
                    dto.setInitiator(initiatorDto);
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findByIdNew(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        if (request.stateAction() != null) {
            StateActionAdmin stateActionAdmin = request.stateAction();
            EventState currentState = event.getState();

            if (stateActionAdmin == StateActionAdmin.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING)
                    throw new ConflictException(
                            "Событие можно опубликовать только если оно в состоянии ожидания публикации");
                validateDateEvent(event.getEventDate(), 1);
                event.publish();
            }
            if (stateActionAdmin == StateActionAdmin.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED)
                    throw new ConflictException("Событие можно отклонить пока оно не опубликовано");
                event.canceled();
            }
        }

        Category category = null;
        if (request.category() != null) {
            category = categoryRepository.findById(request.category())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.category() + " не найдена"));
        }

        eventMapper.updateEventFromAdminRequest(request, event, category);
        Event updatedEvent = eventRepository.save(event);

        log.info("Обновлено событие с id={}, title={}, initiatorId={}",
                updatedEvent.getId(), updatedEvent.getTitle(), updatedEvent.getInitiatorId());

        return buildFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsPublic(
            SearchEventPublicRequest request, Pageable pageable, String ip) {

        validateRangeStartAndEnd(request.rangeStart(), request.rangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.text() != null && !request.text().trim().isEmpty())
            specification = specification.and(SearchEventSpecifications.addLikeText(request.text()));
        if (request.categories() != null && !request.categories().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.categories()));
        if (request.paid() != null)
            specification = specification.and(SearchEventSpecifications.isPaid(request.paid()));
        LocalDateTime rangeStart = (request.rangeStart() == null && request.rangeEnd() == null) ?
                LocalDateTime.now() : request.rangeStart();
        if (rangeStart != null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(rangeStart));
        if (request.rangeEnd() != null)
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.rangeEnd()));
        if (request.onlyAvailable())
            specification = specification.and(SearchEventSpecifications.addWhereAvailableSlots());

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        EventStatistics stats = getEventStatistics(eventIds);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests((long) stats.confirmedRequests()
                            .getOrDefault(event.getId(), 0L));

                    dto.setViews(stats.views().getOrDefault(event.getId(), 0L));

                    UserShortDto initiatorDto = userClient.getUser(event.getInitiatorId());
                    dto.setInitiator(initiatorDto);
                    return dto;
                })
                .toList();

        saveHit("/events", ip);

        if (SortState.VIEWS.equals(request.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .toList();
        } else if (SortState.EVENT_DATE.equals(request.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, String ip) {
        Event event = eventRepository.findByIdNew(eventId)
                .filter(ev -> ev.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        saveHit("/events/" + eventId, ip);

        return buildFullDto(event);
    }

    private EventFullDto buildFullDto(Event event) {
        Map<Long, Long> confirmedRequests = getConfirmedRequests(List.of(event.getId()));
        Map<Long, Long> views = getViewsForEvents(List.of(event.getId()));

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests((long) confirmedRequests.getOrDefault(event.getId(), 0L));
        dto.setViews(views.getOrDefault(event.getId(), 0L));

        UserShortDto initiatorDto = userClient.getUser(event.getInitiatorId());
        dto.setInitiator(initiatorDto);

        return dto;
    }

    private void validateRangeStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new BadRequestException("Дата начала не может быть позже даты окончания");
    }

    private void saveHit(String path, String ip) {
        CreateEndpointHitDto endpointHitDto = new CreateEndpointHitDto(
                "main-service", path, ip, LocalDateTime.now());
        statsClient.saveHit(endpointHitDto);
    }

    private void validateDateEvent(LocalDateTime eventDate, long minHoursBeforeStartEvent) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(minHoursBeforeStartEvent)))
            throw new ValidationException(
                    "Дата начала события не может быть ранее чем через " + minHoursBeforeStartEvent + " часа(ов)");
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {  // ← Integer → Long
        if (eventIds.isEmpty()) return Map.of();

        Map<Long, Long> confirmedRequests = requestClient.getConfirmedRequestsCount(eventIds);

        Map<Long, Long> result = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));

        result.putAll(confirmedRequests);

        return result;
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();
        Event firstEvent = eventRepository.findFirstByOrderByCreatedOnAsc();
        LocalDateTime start = firstEvent != null ? firstEvent.getCreatedOn() : LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        Map<Long, Long> views = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));

        if (stats != null && !stats.isEmpty()) {
            stats.forEach(stat -> {
                Long eventId = getEventIdFromUri(stat.uri());
                if (eventId > -1L) {
                    views.put(eventId, stat.hits());
                }
            });
        }
        return views;
    }

    private Long getEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.substring("/events".length() + 1));
        } catch (Exception e) {
            return -1L;
        }
    }

    private EventStatistics getEventStatistics(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return new EventStatistics(Map.of(), Map.of());
        }

        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);  // ← теперь Long
        Map<Long, Long> views = getViewsForEvents(eventIds);

        return new EventStatistics(confirmedRequests, views);
    }
}
