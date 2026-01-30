package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;

import java.util.List;

public interface EventService {
    List<EventShortDto> getEvents(Long userId, Pageable pageable);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEvent(Long userId, Long eventId, String ip);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    Event getEventOrThrow(Long eventId);

    List<EventFullDto> getEventsAdmin(SearchEventAdminRequest request, Pageable pageable);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getEventsPublic(SearchEventPublicRequest requestParams, Pageable pageable, String ip);

    EventFullDto getEventByIdPublic(Long eventId, String ip);
}
