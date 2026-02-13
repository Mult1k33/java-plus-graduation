package ru.yandex.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.service.EventService;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsPublic(@ModelAttribute @Valid SearchEventPublicRequest request,
                                               HttpServletRequest httpRequest
    ) {
        int size = (request.size() != null && request.size() > 0) ? request.size() : 10;
        int from = request.from() != null ? request.from() : 0;
        PageRequest pageRequest = PageRequest.of(from / size, size);
        log.debug("Controller: getEventsPublic filters={}", request);
        return eventService.getEventsPublic(request, pageRequest, httpRequest.getRemoteAddr());
    }

    @GetMapping("/{id}")
    public EventFullDto getEventByIdPublic(@PathVariable("id") @Positive Long eventId,
                                           HttpServletRequest httpRequest,
                                           @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.debug("Controller: getEventByIdPublic eventId={}", eventId);
        return eventService.getEventByIdPublic(eventId, httpRequest.getRemoteAddr(), userId);
    }

    @GetMapping("/recommendations")
    public Stream<RecommendedEventProto> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @RequestParam(defaultValue = "10") int maxResults) {

        log.debug("Controller: getRecommendations userId={}, maxResults={}", userId, maxResults);
        return eventService.getRecommendationsForUser(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId) {

        log.debug("Controller: likeEvent eventId={}, userId={}", eventId, userId);
        eventService.likeEvent(userId, eventId);
    }
}
