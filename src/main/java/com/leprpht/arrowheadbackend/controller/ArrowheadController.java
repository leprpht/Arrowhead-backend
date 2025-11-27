package com.leprpht.arrowheadbackend.controller;

import com.leprpht.arrowheadbackend.model.DailyEnergyAverage;
import com.leprpht.arrowheadbackend.model.EnergyMixPeriod;
import com.leprpht.arrowheadbackend.service.ArrowheadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
        LocalDate todayUtc = LocalDate.now(Clock.systemUTC());

        Instant from = todayUtc.atStartOfDay().toInstant(ZoneOffset.UTC).plusSeconds(1);
        Instant to = todayUtc.plusDays(2).atTime(23, 30).toInstant(ZoneOffset.UTC);

        CompletableFuture<List<DailyEnergyAverage>> future = arrowheadService.fetchDailyAveragesAsync(from, to);
        return toResponseFuture(future);
    }

    private <T> CompletableFuture<ResponseEntity<List<T>>> toResponseFuture(CompletableFuture<List<T>> future) {
        return future
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.emptyList()));
    }
}
