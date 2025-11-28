package com.leprpht.arrowheadbackend.service;

import com.leprpht.arrowheadbackend.model.*;
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

    private static final Set<String> CLEAN_FUELS = Set.of("biomass", "nuclear", "hydro", "wind", "solar");

    public ArrowheadService(WebClient.Builder webClientBuilder,
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
                .map(response -> Optional.ofNullable(response)
                        .map(EnergyApiResponse::getData)
                        .orElse(Collections.emptyList()))
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(Collections.emptyList());
    }

    private Mono<List<DailyEnergyAverage>> fetchDailyAverages(Instant from, Instant to) {
        return fetchEnergyData(from.toString(), to.toString())
                .map(this::groupPeriodsByUtcDay)
                .onErrorReturn(Collections.emptyList());
    }

    private List<DailyEnergyAverage> groupPeriodsByUtcDay(List<EnergyMixPeriod> periods) {
        if (periods == null || periods.isEmpty()) return Collections.emptyList();

        Map<LocalDate, Map<String, DoubleSummaryStatistics>> dailyStats = new TreeMap<>();

        for (EnergyMixPeriod period : periods) {
            if (period == null || period.getFrom() == null || period.getGenerationMix() == null) continue;

            LocalDate dateUtc = period.getFrom().atZone(ZoneOffset.UTC).toLocalDate();

            for (EnergyMix mix : period.getGenerationMix()) {
                if (mix == null || mix.getFuel() == null) continue;
                dailyStats.computeIfAbsent(dateUtc, d -> new HashMap<>())
                        .computeIfAbsent(mix.getFuel(), f -> new DoubleSummaryStatistics())
                        .accept(mix.getPerc());
            }
        }

        List<DailyEnergyAverage> results = new ArrayList<>(dailyStats.size());
        for (Map.Entry<LocalDate, Map<String, DoubleSummaryStatistics>> entry : dailyStats.entrySet()) {
            LocalDate date = entry.getKey();
            List<EnergyMix> averages = entry.getValue().entrySet().stream()
                    .map(e -> EnergyMix.builder()
                            .fuel(e.getKey())
                            .perc(e.getValue().getAverage())
                            .build())
                    .sorted(Comparator.comparing(EnergyMix::getFuel))
                    .toList();
            double totalCleanPerc = averages.stream()
                    .filter(mix -> CLEAN_FUELS.contains(mix.getFuel()))
                    .mapToDouble(EnergyMix::getPerc)
                    .sum();
            results.add(new DailyEnergyAverage(date, averages, totalCleanPerc));
        }

        return results;
    }

    private CompletableFuture<List<EnergyMixPeriod>> fetchEnergyDataAsync(String from, String to) {
        return fetchEnergyData(from, to).toFuture();
    }

    public CompletableFuture<ChargingTime> getOptimalChargingTime(int hours, Instant from, Instant to) {
        if (hours < 1 || hours > 6) throw new IllegalArgumentException();

        return fetchEnergyDataAsync(from.toString(), to.toString())
                .thenApplyAsync(periods -> findOptimalChargingTime(hours, from, to, periods));
    }

    private ChargingTime findOptimalChargingTime(int hours, Instant from, Instant to, List<EnergyMixPeriod> periods) {
        Instant timeBracket = from.plusSeconds(hours * 3600L);

        ChargingTime best = ChargingTime.builder()
                .from(from)
                .to(from.plusSeconds(hours * 3600L))
                .perc(0.0)
                .build();

        while (timeBracket.isBefore(to)) {
            double cleanPerc = calculateCleanPercentage(hours, timeBracket, periods);
            if (cleanPerc > best.getPerc()) {
                Instant bracketStart = timeBracket.minusSeconds(hours * 3600L);
                best.setFrom(bracketStart);
                best.setTo(timeBracket);
                best.setPerc(cleanPerc);
            }
            timeBracket = timeBracket.plusSeconds(30 * 60);
        }

        return best;
    }

    private double calculateCleanPercentage(int hours, Instant timeBracket, List<EnergyMixPeriod> periods) {
        Instant bracketStart = timeBracket.minusSeconds(hours * 3600L);
        double cleanPerc = 0.0;

        for (EnergyMixPeriod period : periods) {
            if (period.getFrom().isBefore(bracketStart) || period.getTo().isAfter(timeBracket))
                continue;

            for (EnergyMix mix : period.getGenerationMix()) {
                if (mix != null && mix.getFuel() != null && CLEAN_FUELS.contains(mix.getFuel())) {
                    cleanPerc += mix.getPerc();
                }
            }
        }

        return cleanPerc / (hours * 2);
    }
}
