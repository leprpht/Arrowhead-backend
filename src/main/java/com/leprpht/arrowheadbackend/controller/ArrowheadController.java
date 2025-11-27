package com.leprpht.arrowheadbackend.controller;

import com.leprpht.arrowheadbackend.model.EnergyMixPeriod;
import com.leprpht.arrowheadbackend.service.ArrowheadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        return arrowheadService.fetchEnergyDataAsync(from, to)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Collections.emptyList())
                );
    }
}
