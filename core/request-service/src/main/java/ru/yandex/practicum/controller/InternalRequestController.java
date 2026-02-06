package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.enums.RequestStatus;
import ru.yandex.practicum.repository.RequestRepository;
import ru.yandex.practicum.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController {

    private final RequestRepository requestRepository;
    private final RequestService requestService;

    @GetMapping("/event/{eventId}/count/{status}")
    public Long countByStatus(@PathVariable Long eventId, @PathVariable RequestStatus status) {
        return requestRepository.countByEventIdAndStatus(eventId, status);
    }

    @PostMapping("/events/count")
    public Map<Long, Long> getConfirmedRequestsCount(@RequestBody List<Long> eventIds) {
        return requestService.getConfirmedRequestsForEvents(eventIds);
    }
}
