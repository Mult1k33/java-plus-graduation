package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.request.*;

import java.util.List;
import java.util.Map;

public interface RequestService {
    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId,
                                                       Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds);
}
