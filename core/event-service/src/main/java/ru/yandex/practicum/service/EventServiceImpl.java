package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.yandex.practicum.client.AnalyzerClient;
import ru.yandex.practicum.client.CollectorClient;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final CategoryRepository categoryRepository;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateDateEvent(newEventDto.eventDate(), 2);

        checkUserExists(userId);

        Category category = categoryRepository.findById(newEventDto.category())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.category() + " не найдена"));

        Event event = eventMapper.toEntity(newEventDto, userId, category);
        event.setRating(0.0);
        event = eventRepository.save(event);

        log.info("Создано событие с id={}, title={}, initiatorId={}", event.getId(), event.getTitle(), userId);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setInitiator(getUserShortDto(userId));
        dto.setConfirmedRequests(0L);
        dto.setRating(0.0);
        return dto;
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        checkUserExists(userId);

        List<Event> events = eventRepository.findAllByInitiatorIdOrderByCreatedOnDesc(userId, pageable);

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);
        Map<Long, Double> ratings = getRatingsForEvents(eventIds);

        Map<Long, UserShortDto> userCache = new HashMap<>();

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));

                    UserShortDto initiatorDto = userCache.computeIfAbsent(
                            event.getInitiatorId(),
                            this::getUserShortDto
                    );
                    dto.setInitiator(initiatorDto);
                    return dto;
                })
                .toList();
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId, String ip) {
        try {
            userClient.getUser(userId);
            Event event = getEventOrThrow(eventId);

            if (!event.getInitiatorId().equals(userId)) {
                throw new NotFoundException("Событие c id " + eventId + " не найдено у пользователя с id " + userId);
            }

            return buildFullDto(event);

        } catch (Exception e) {
            log.warn("Сервис пользователей недоступен или пользователь не найден, userId={}", userId);
            Event event = getEventOrThrow(eventId);
            return buildFullDto(event);
        }
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        checkUserExists(userId);

        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId))
            throw new ValidationException("Пользователь не является инициатором события и не может его редактировать");

        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationException("Изменять можно только не опубликованные события");

        if (request.eventDate() != null) {
            validateDateEvent(request.eventDate(), 2);
        }

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
        return eventRepository.findByIdNew(eventId)
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

        if (eventsPage.isEmpty()) {
            return List.of();
        }

        List<Event> events = eventsPage.getContent();
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);
        Map<Long, Double> ratings = getRatingsForEvents(eventIds);
        Map<Long, UserShortDto> userCache = new HashMap<>();

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));

                    UserShortDto initiatorDto = userCache.computeIfAbsent(
                            event.getInitiatorId(),
                            this::getUserShortDto
                    );
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
                if (event.getEventDate() != null) {
                    validateDateEvent(event.getEventDate(), 1);
                }
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

        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);
        Map<Long, Double> ratings = getRatingsForEvents(eventIds);
        Map<Long, UserShortDto> userCache = new HashMap<>();

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));

                    UserShortDto initiatorDto = userCache.computeIfAbsent(
                            event.getInitiatorId(),
                            this::getUserShortDto
                    );
                    dto.setInitiator(initiatorDto);
                    return dto;
                })
                .toList();

        if (SortState.VIEWS.equals(request.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getRating).reversed())
                    .toList();
        } else if (SortState.EVENT_DATE.equals(request.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, String ip, Long userId) {
        Event event = eventRepository.findByIdNew(eventId)
                .filter(ev -> ev.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.VIEW)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();

        collectorClient.sendUserAction(action);

        return buildFullDto(event);
    }

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(Long userId, int maxResults) {
        log.info("Получение рекомендаций для пользователя userId={}, maxResults={}", userId, maxResults);

        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        return analyzerClient.getRecommendationsForUser(request);
    }

    @Override
    @Transactional
    public void likeEvent(Long userId, Long eventId) {
        log.info("Лайк события eventId={} от пользователя userId={}", eventId, userId);

        Event event = getEventOrThrow(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя лайкнуть неопубликованное событие");
        }

        if (!hasUserVisitedEvent(userId, eventId)) {
            throw new ValidationException("Нельзя лайкнуть непосещенное мероприятие");
        }

        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.LIKE)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();

        collectorClient.sendUserAction(action);
    }

    private EventFullDto buildFullDto(Event event) {
        Map<Long, Long> confirmedRequests = getConfirmedRequests(List.of(event.getId()));
        Double rating = getEventRating(event.getId());

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
        dto.setRating(rating);

        UserShortDto initiatorDto = getUserShortDto(event.getInitiatorId());
        dto.setInitiator(initiatorDto);

        return dto;
    }

    private void validateRangeStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new BadRequestException("Дата начала не может быть позже даты окончания");
    }

    private Double getEventRating(Long eventId) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addEventId(eventId)
                    .build();

            return analyzerClient.getInteractionsCount(request)
                    .findFirst()
                    .map(RecommendedEventProto::getScore)
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("Не удалось получить рейтинг для события {}", eventId, e);
            return 0.0;
        }
    }

    private Map<Long, Double> getRatingsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        Map<Long, Double> ratings = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0.0));

        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            analyzerClient.getInteractionsCount(request)
                    .forEach(proto -> ratings.put(proto.getEventId(), proto.getScore()));
        } catch (Exception e) {
            log.warn("Не удалось получить рейтинги для событий", e);
        }

        return ratings;
    }

    private boolean hasUserVisitedEvent(Long userId, Long eventId) {
        try {
            return requestClient.hasUserConfirmedRequest(userId, eventId);
        } catch (Exception e) {
            log.warn("Не удалось проверить посещение события", e);
            return false;
        }
    }

    private void validateDateEvent(LocalDateTime eventDate, long minHoursBeforeStartEvent) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(minHoursBeforeStartEvent)))
            throw new ValidationException(
                    "Дата начала события не может быть ранее чем через " + minHoursBeforeStartEvent + " часа(ов)");
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        Map<Long, Long> result = new HashMap<>();

        for (Long eventId : eventIds) {
            try {
                Long count = requestClient.countByStatus(eventId, RequestStatus.CONFIRMED);
                result.put(eventId, count != null ? count : 0L);
                log.debug("Получено {} подтвержденных запросов для события {}", count, eventId);
            } catch (Exception e) {
                log.warn("Не удалось получить подтвержденные запросы для события {}: {}",
                        eventId, e.getMessage());
                result.put(eventId, 0L);
            }
        }

        return result;
    }

    private void checkUserExists(Long userId) {
        try {
            userClient.getUser(userId);
        } catch (Exception e) {
            log.warn("Сервис пользователей недоступен, пропускаем проверку. userId={}", userId);
        }
    }

    private UserShortDto getUserShortDto(Long userId) {
        try {
            return userClient.getUser(userId);
        } catch (Exception e) {
            log.warn("Сервис пользователей недоступен, используем заглушку. userId={}", userId);
            return new UserShortDto(userId, "Пользователь-" + userId);
        }
    }
}
