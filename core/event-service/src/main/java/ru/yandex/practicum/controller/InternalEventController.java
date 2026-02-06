package ru.yandex.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.UserClient;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.EventMapper;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.repository.EventRepository;

@Slf4j
@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable @Positive Long eventId) {
        log.debug("Controller: getEvent eventId={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setInitiator(userClient.getUser(event.getInitiatorId()));

        return dto;
    }
}
