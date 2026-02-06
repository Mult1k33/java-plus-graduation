package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.request.*;
import ru.yandex.practicum.enums.RequestStatus;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "request-service",
        path = "/internal/requests",
        fallback = RequestClientFallback.class
)
public interface RequestClient {

    @GetMapping("/event/{eventId}/count/{status}")
    Long countByStatus(@PathVariable Long eventId, @PathVariable RequestStatus status);

    @PostMapping("/events/count")
    Map<Long, Long> getConfirmedRequestsCount(@RequestBody List<Long> eventIds);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId, @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request
    );
}
