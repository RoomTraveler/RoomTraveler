package com.ssafy.trip.stats.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Schema(description = "수동 통계 집계 요청 DTO")
public class ManualAggregationRequest {

    @NotNull(message = "시작일은 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "집계 시작일 (YYYY-MM-DD)", example = "2023-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "집계 종료일 (YYYY-MM-DD)", example = "2023-01-07", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @NotEmpty(message = "기간 유형은 필수입니다.")
    @Pattern(regexp = "WEEKLY|MONTHLY", message = "기간 유형은 WEEKLY 또는 MONTHLY여야 합니다.")
    @Schema(description = "기간 유형 (WEEKLY, MONTHLY)", example = "WEEKLY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String periodType;

    @Schema(description = "상위 N개 랭킹 (기본값: 10)", example = "10", defaultValue = "10")
    private int topN = 10;
} 