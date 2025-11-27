package com.leprpht.arrowheadbackend.controller;

import com.leprpht.arrowheadbackend.model.EnergyMixPeriod;
import com.leprpht.arrowheadbackend.service.ArrowheadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/energyMix")
    public CompletableFuture<ResponseEntity<List<EnergyMixPeriod>>> getEnergy(
            @RequestParam String from,
            @RequestParam String to) {

        return fetchEnergyDataResponse(from, to);
    }

    @GetMapping("/prognosis")
    public CompletableFuture<ResponseEntity<List<EnergyMixPeriod>>> getPrognosis() {
        LocalDate today = LocalDate.now();

        Instant from = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = today.plusDays(2).atTime(23, 30).toInstant(ZoneOffset.UTC);

        String fromStr = from.toString();
        String toStr = to.toString();

        return fetchEnergyDataResponse(fromStr, toStr);
    }

    private CompletableFuture<ResponseEntity<List<EnergyMixPeriod>>> fetchEnergyDataResponse(String from, String to) {
        return arrowheadService.fetchEnergyDataAsync(from, to)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Collections.emptyList())
                );
    }
}
