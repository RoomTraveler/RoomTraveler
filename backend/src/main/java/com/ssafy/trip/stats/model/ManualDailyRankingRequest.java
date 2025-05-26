package com.ssafy.trip.stats.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "수동 일일 랭킹 업데이트 요청 DTO")
public class ManualDailyRankingRequest {

    @NotNull(message = "날짜는 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "랭킹 기준 날짜 (YYYY-MM-DD)", example = "2023-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Schema(description = "상위 N개 랭킹 (기본값: 10)", example = "10", defaultValue = "10")
    private int topN = 10;
} 