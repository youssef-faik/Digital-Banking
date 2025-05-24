package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.DashboardStatsDTO;
import com.example.digitalbanking.dtos.DashboardChartDataDTO; // Added import
import com.example.digitalbanking.services.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
@Slf4j
@Tag(name = "Dashboard API", description = "Endpoints for dashboard statistics and data")
public class DashboardController {

    private final BankAccountService bankAccountService;

    @Operation(summary = "Get Dashboard Statistics", description = "Retrieves key statistics for the authenticated user's dashboard, such as total customers, accounts, balances, and operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard statistics",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardStatsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching statistics",
                    content = @Content)
    })
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        log.info("REST request to get dashboard statistics");
        DashboardStatsDTO stats = bankAccountService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get Dashboard Chart Data", description = "Retrieves data formatted for charts on the authenticated user's dashboard, such as operation trends and account type distributions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard chart data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardChartDataDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching chart data",
                    content = @Content)
    })
    @GetMapping("/chart-data")
    public ResponseEntity<DashboardChartDataDTO> getDashboardChartData() {
        log.info("REST request to get dashboard chart data");
        DashboardChartDataDTO chartData = bankAccountService.getDashboardChartData();
        return ResponseEntity.ok(chartData);
    }
}
