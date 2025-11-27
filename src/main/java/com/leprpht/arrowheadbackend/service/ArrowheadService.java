package com.leprpht.arrowheadbackend.service;

import com.leprpht.arrowheadbackend.model.DailyEnergyAverage;
import com.leprpht.arrowheadbackend.model.EnergyApiResponse;
import com.leprpht.arrowheadbackend.model.EnergyMix;
import com.leprpht.arrowheadbackend.model.EnergyMixPeriod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ArrowheadService {

    private final WebClient webClient;
    private final String baseUrl;

    public ArrowheadService(
            WebClient.Builder webClientBuilder,
            @Value("${MIX_API_URL}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    public CompletableFuture<List<DailyEnergyAverage>> fetchDailyAveragesAsync(Instant from, Instant to) {
        return fetchDailyAverages(from, to).toFuture();
    }

    private Mono<List<EnergyMixPeriod>> fetchEnergyData(String from, String to) {
        String url = baseUrl + from.trim() + "/" + to.trim();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(EnergyApiResponse.class)
                .map(response -> (response == null || response.getData() == null)
                        ? Collections.<EnergyMixPeriod>emptyList()
                        : response.getData())
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(Collections.emptyList());
    }

    private Mono<List<DailyEnergyAverage>> fetchDailyAverages(Instant from, Instant to) {
        String fromStr = from.toString();
        String toStr = to.toString();

        return fetchEnergyData(fromStr, toStr)
                .map(this::groupPeriodsByUtcDay)
                .onErrorReturn(Collections.emptyList());
    }

    private List<DailyEnergyAverage> groupPeriodsByUtcDay(List<EnergyMixPeriod> periods) {
        if (periods == null || periods.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocalDate, Map<String, DoubleSummaryStatistics>> accumulator = new TreeMap<>();

        for (EnergyMixPeriod period : periods) {
            if (period == null || period.getFrom() == null || period.getGenerationMix() == null) continue;

            LocalDate dateUtc = period.getFrom().atZone(ZoneOffset.UTC).toLocalDate();

            for (EnergyMix mix : period.getGenerationMix()) {
                if (mix == null || mix.getFuel() == null) continue;

                accumulator.computeIfAbsent(dateUtc, d -> new HashMap<>())
                        .computeIfAbsent(mix.getFuel(), f -> new DoubleSummaryStatistics())
                        .accept(mix.getPerc());
            }
        }

        List<DailyEnergyAverage> results = new ArrayList<>(accumulator.size());
        for (Map.Entry<LocalDate, Map<String, DoubleSummaryStatistics>> entry : accumulator.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, DoubleSummaryStatistics> fuelStats = entry.getValue();

            List<EnergyMix> averages = fuelStats.entrySet().stream()
                    .map(e -> EnergyMix.builder()
                            .fuel(e.getKey())
                            .perc(e.getValue().getAverage())
                            .build())
                    .sorted(Comparator.comparing(EnergyMix::getFuel))
                    .toList();

            results.add(new DailyEnergyAverage(date, averages));
        }

        return results;
    }
}
