package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.enums.RequestStatus;

@FeignClient(
        name = "request-service",
        path = "/internal/requests",
        fallback = RequestClientFallback.class
)
public interface RequestClient {

    @GetMapping("/event/{eventId}/count/{status}")
    Long countByStatus(@PathVariable Long eventId, @PathVariable RequestStatus status);
}
