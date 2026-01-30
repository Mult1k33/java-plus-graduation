package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.CreateEndpointHitDto;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.service.StatsService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@RequestBody @Valid CreateEndpointHitDto createEndpointHitDto) {
        log.debug("Controller: createHit requestBody={}", createEndpointHitDto);
        statsService.createHit(createEndpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam("start") String start, @RequestParam("end") String end,
                                       @RequestParam(value = "uris", required = false) List<String> uris,
                                       @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        StatsRequest request = StatsRequest.of(start, end, uris, unique);

        if (request.start().isAfter(request.end())) {
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        log.debug("Controller: getStats request={}", request);
        return statsService.getStats(request);
    }
}
