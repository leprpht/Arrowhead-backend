package com.leprpht.arrowheadbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingTime {
    private Instant from;
    private Instant to;
    private double perc;
}
