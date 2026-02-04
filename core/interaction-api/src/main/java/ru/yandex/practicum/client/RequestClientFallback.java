package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.enums.RequestStatus;

@Component
@Slf4j
public class RequestClientFallback implements RequestClient {

    @Override
    public Long countByStatus(Long eventId, RequestStatus status) {
        log.warn("Сервис запросов недоступен, eventId={}, status={}", eventId, status);
        return 0L;
    }
}
