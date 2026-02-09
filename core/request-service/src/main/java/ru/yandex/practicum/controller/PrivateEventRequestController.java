package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.request.*;
import ru.yandex.practicum.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateEventRequestController {
    private final RequestService requestService;

    @PatchMapping
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest request) {

        log.debug("Controller: updateRequestStatus userId={}, eventId={}, request={}",
                userId, eventId, request);

        return requestService.updateRequestStatus(userId, eventId, request);
    }

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {

        log.debug("Controller: getEventRequests userId={}, eventId={}", userId, eventId);
        return requestService.getRequestsByEvent(userId, eventId);
    }
}
