package ru.practicum.request.dto;

import ru.practicum.request.model.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
      List<Long> requestIds,
      RequestStatus status
) {
}
