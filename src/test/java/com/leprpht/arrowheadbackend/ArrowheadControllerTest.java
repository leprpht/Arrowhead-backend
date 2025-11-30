package com.leprpht.arrowheadbackend;

import com.leprpht.arrowheadbackend.controller.ArrowheadController;
import com.leprpht.arrowheadbackend.model.ChargingTime;
import com.leprpht.arrowheadbackend.model.DailyEnergyAverage;
import com.leprpht.arrowheadbackend.model.EnergyMix;
import com.leprpht.arrowheadbackend.service.ArrowheadService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArrowheadControllerTest {

    private ArrowheadService arrowheadService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        arrowheadService = Mockito.mock(ArrowheadService.class);
        ArrowheadController controller = new ArrowheadController(arrowheadService);

        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testPrognosisEndpointReturnsData() {
        DailyEnergyAverage average = DailyEnergyAverage.builder()
                .date(LocalDate.now().plusDays(1))
                .averages(List.of(
                                EnergyMix.builder().fuel("biomass").perc(50.0).build(),
                                EnergyMix.builder().fuel("coal").perc(21.1).build(),
                                EnergyMix.builder().fuel("imports").perc(0.0).build(),
                                EnergyMix.builder().fuel("gas").perc(0.0).build(),
                                EnergyMix.builder().fuel("nuclear").perc(0.0).build(),
                                EnergyMix.builder().fuel("other").perc(7.0).build(),
                                EnergyMix.builder().fuel("hydro").perc(0.0).build(),
                                EnergyMix.builder().fuel("solar").perc(3.0).build(),
                                EnergyMix.builder().fuel("wind").perc(0.0).build()
                ))
                .totalCleanPerc(60.1)
                .build();

        Mockito.when(arrowheadService.fetchDailyAveragesAsync(Mockito.any(), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(average)));

        webTestClient.get()
                .uri("/api/prognosis")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DailyEnergyAverage.class)
                .hasSize(1)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    DailyEnergyAverage returned = response.getResponseBody().getFirst();
                    assert returned.getTotalCleanPerc() == 60.1;
                });
    }

    @Test
    void testChargingTimeEndpointReturnsData() {
        Instant from = Instant.now();
        Instant to = from.plusSeconds(3 * 3600L);
        ChargingTime chargingTime = ChargingTime.builder()
                .from(from)
                .to(to)
                .perc(75.0)
                .build();

        Mockito.when(arrowheadService.getOptimalChargingTime(Mockito.anyInt(), Mockito.any(), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(chargingTime));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/chargingTime")
                        .queryParam("hours", 3).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChargingTime.class)
                .consumeWith(response -> {
                    ChargingTime returned = response.getResponseBody();
                    assert returned.getPerc() == 75.0;
                });
    }
}
