package com.leprpht.arrowheadbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyEnergyAverage {
    private LocalDate date;
    private List<EnergyMix> averages;
}
