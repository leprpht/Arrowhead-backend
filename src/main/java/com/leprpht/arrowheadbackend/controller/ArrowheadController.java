package com.leprpht.arrowheadbackend.controller;

import com.leprpht.arrowheadbackend.model.ChargingTime;
import com.leprpht.arrowheadbackend.model.DailyEnergyAverage;
import com.leprpht.arrowheadbackend.service.ArrowheadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class ArrowheadController {

    private final ArrowheadService arrowheadService;

    public ArrowheadController(ArrowheadService arrowheadService) {
        this.arrowheadService = arrowheadService;
    }

    @GetMapping("/prognosis")
    public CompletableFuture<ResponseEntity<List<DailyEnergyAverage>>> getPrognosis() {
        InstantRange range = getNextTwoDaysRange();
        CompletableFuture<List<DailyEnergyAverage>> future =
                arrowheadService.fetchDailyAveragesAsync(range.from(), range.to());
        return toResponseFuture(future);
    }

    @GetMapping("/chargingTime")
    public CompletableFuture<ResponseEntity<ChargingTime>> getOptimalChargingTime(@RequestParam int hours) {
        InstantRange range = getNextTwoDaysRange();
        CompletableFuture<ChargingTime> future =
                arrowheadService.getOptimalChargingTime(hours, range.from(), range.to());
        return future
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
    }

    private <T> CompletableFuture<ResponseEntity<List<T>>> toResponseFuture(CompletableFuture<List<T>> future) {
        return future
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.emptyList()));
    }

    private InstantRange getNextTwoDaysRange() {
        ZoneId utc = ZoneId.of("UTC");
        LocalDate todayUTC = LocalDate.now(utc);

        Instant from = todayUTC.atStartOfDay(utc).plusHours(1).toInstant();
        Instant to = todayUTC.plusDays(2).atTime(23, 30).atZone(utc).toInstant();
        return new InstantRange(from, to);
    }
}
