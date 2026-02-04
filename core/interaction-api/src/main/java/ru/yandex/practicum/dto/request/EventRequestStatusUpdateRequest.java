package ru.yandex.practicum.dto.request;

import ru.yandex.practicum.enums.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
      List<Long> requestIds,
      RequestStatus status
) {
}
