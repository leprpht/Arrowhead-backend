package com.leprpht.arrowheadbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyMixPeriod {
    private String from;
    private String to;
    @JsonProperty("generationmix")
    private List<EnergyMix> generationMix;
}
