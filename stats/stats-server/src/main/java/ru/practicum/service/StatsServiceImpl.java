package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.CreateEndpointHitDto;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper hitMapper;

    @Override
    @Transactional
    public void createHit(CreateEndpointHitDto createEndpointHitDto) {
        EndpointHit hit = hitMapper.fromNewRequest(createEndpointHitDto);
        hit = statsRepository.save(hit);
        log.info("Отправлен запрос на сохранение информации id={}", hit.getId());
    }

    @Override
    public List<ViewStatsDto> getStats(StatsRequest request) {
        List<String> uris = (request.uris() == null || request.uris().isEmpty())
                ? null
                : request.uris();

        List<ViewStatsDto> result = request.unique()
                ? statsRepository.getUniqueStats(request.start(), request.end(), uris)
                : statsRepository.getStats(request.start(), request.end(), uris);

        log.info("Размер полученного списка статистики: {}", result.size());
        return result;
    }
}
