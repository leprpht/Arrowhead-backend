package com.leprpht.arrowheadbackend.service;

import com.leprpht.arrowheadbackend.model.EnergyApiResponse;
import com.leprpht.arrowheadbackend.model.EnergyMixPeriod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
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

    public CompletableFuture<List<EnergyMixPeriod>> fetchEnergyDataAsync(String from, String to) {
        return fetchEnergyData(from, to).toFuture();
    }
}
