package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.config.FeignRetryConfig;
import ru.yandex.practicum.dto.event.EventFullDto;

@FeignClient(
        name = "event-service",
        path = "/internal/events",
        configuration = FeignRetryConfig.class,
        fallback = EventClientFallback.class
)
public interface EventClient {

    @GetMapping("/{eventId}")
    EventFullDto getEvent(@PathVariable Long eventId);
}
