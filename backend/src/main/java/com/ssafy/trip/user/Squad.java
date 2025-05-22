package com.ssafy.trip.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Squad {
    private Long squadId;
    private String squadName;
    private LocalDateTime createdAt;
    private Long createdBy;
}
