package ru.yandex.practicum.request.dto;

import ru.yandex.practicum.request.model.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
      List<Long> requestIds,
      RequestStatus status
) {
}
