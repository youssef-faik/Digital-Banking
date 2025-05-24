package com.example.digitalbanking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartDataDTO {
    private List<DataPointDTO> operationsTrend; // Time-series data for operations (e.g., daily debit/credit counts)
    private List<DataPointDTO> accountTypeDistribution; // Changed to List<DataPointDTO>
}
