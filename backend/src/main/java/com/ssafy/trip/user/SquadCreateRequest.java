package com.ssafy.trip.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadCreateRequest {
    private String squadName;
    private List<Long> invitedUserIds;
}
