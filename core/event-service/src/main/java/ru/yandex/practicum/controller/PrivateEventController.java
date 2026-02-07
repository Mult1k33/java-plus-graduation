package ru.yandex.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable("userId") @Positive Long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.debug("Controller: getEvents userId={}, pageable={}", userId, pageable);
        return eventService.getEvents(userId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable("userId") @Positive Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto
    ) {
        log.debug("Controller: createEvent userId={}, data={}", userId, newEventDto);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable("userId") @Positive Long userId,
                                 @PathVariable("eventId") @Positive Long eventId,
                                 HttpServletRequest request
    ) {
        log.debug("Controller: getEvent userId={}, eventId={}", userId, eventId);
        return eventService.getEvent(userId, eventId, request.getRemoteAddr());
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable("userId") @Positive Long userId,
                                    @PathVariable("eventId") @Positive Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest request
    ) {
        log.debug("Controller: updateEvent userId={}, eventId={}, data={}", userId, eventId, request);
        return eventService.updateEvent(userId, eventId, request);
    }
}
