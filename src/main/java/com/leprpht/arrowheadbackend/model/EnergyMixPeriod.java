package com.leprpht.arrowheadbackend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyMixPeriod {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmX", timezone = "UTC")
    private Instant from;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmX", timezone = "UTC")
    private Instant to;

    @JsonProperty("generationmix")
    private List<EnergyMix> generationMix;
}
