package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.request.*;
import ru.yandex.practicum.enums.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RequestClientFallback implements RequestClient {

    @Override
    public Long countByStatus(Long eventId, RequestStatus status) {
        log.warn("Сервис запросов недоступен, eventId={}, status={}", eventId, status);
        return 0L;
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsCount(List<Long> eventIds) {
        log.warn("Сервис запросов недоступен, eventIds={}", eventIds);
        return eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        log.warn("Сервис запросов недоступен, userId={}, eventId={}", userId, eventId);
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        log.warn("Сервис запросов недоступен, userId={}, eventId={}, request={}", userId, eventId, request);
        return new EventRequestStatusUpdateResult(List.of(), List.of());
    }
}
