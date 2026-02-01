package ru.yandex.practicum.service;

import ru.yandex.practicum.CreateEndpointHitDto;
import ru.yandex.practicum.StatsRequest;
import ru.yandex.practicum.ViewStatsDto;

import java.util.List;

public interface StatsService {
    void createHit(CreateEndpointHitDto createEndpointHitDto);

    List<ViewStatsDto> getStats(StatsRequest request);
}
