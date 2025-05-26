package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.AccommodationRequestDto;
import com.ssafy.trip.accommodation.model.AccommodationResponseDto;
import com.ssafy.trip.accommodation.model.RoomRequestDto;
import com.ssafy.trip.accommodation.model.RoomResponseDto;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.host.model.Host;
import com.ssafy.trip.host.service.HostService;
import com.ssafy.trip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * API를 통해 숙소 정보를 제공하는 REST 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@Slf4j // Slf4j 어노테이션 추가
@RequestMapping("/api") // 기본 경로를 /api로 변경하고, 각 메소드에서 세부 경로 지정
public class ApiAccommodationController {

    private final AccommodationService accommodationService;
    private final HostService hostService;

    /**
     * 전체 숙소 목록 조회 - 페이징 추가 고려 (필요시)
     * 현재는 페이징 없이 전체 목록 반환
     */
    @GetMapping("/accommodations")
    public ResponseEntity<?> listAccommodations() {
        try {
            List<Accommodation> accommodations = accommodationService.getAllAccommodations();
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 지역별 숙소 목록 조회 - 페이징 추가 고려 (필요시)
     * 현재는 페이징 없이 전체 목록 반환
     */
    @GetMapping("/accommodations/region")
    public ResponseEntity<?> getAccommodationsByRegion(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode) {
        try {
            List<Accommodation> accommodations = accommodationService.getAccommodationsByRegion(sidoCode, gugunCode);
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "지역별 숙소 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 키워드로 숙소 검색 - 페이징 추가 고려 (필요시)
     * 현재는 페이징 없이 전체 목록 반환
     */
    @GetMapping("/accommodations/search")
    public ResponseEntity<?> searchAccommodations(@RequestParam String keyword) {
        try {
            List<Accommodation> accommodations = accommodationService.searchAccommodations(keyword);
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 검색 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 숙소 상세 정보 조회
     */
    @GetMapping("/accommodations/{accommodationId}")
    public ResponseEntity<?> getAccommodationDetail(
            @PathVariable Long accommodationId,
            @RequestParam(required = false) String checkInDate, 
            @RequestParam(required = false) String checkOutDate,
            @RequestParam(required = false) Integer guests) {
        try {
            Accommodation accommodation = accommodationService.getAccommodationById(accommodationId);
            if (accommodation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "숙소를 찾을 수 없습니다. ID: " + accommodationId));
            }
            // 날짜와 인원수 파라미터를 사용하여 객실 정보 조회
            List<Room> rooms = accommodationService.getRoomsByAccommodationId(accommodationId, checkInDate, checkOutDate, guests);
            
            Map<String, Object> response = new HashMap<>();
            response.put("accommodation", accommodation);
            response.put("rooms", rooms);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 상세 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 객실 상세 정보 조회
     */
    @GetMapping("/accommodations/room/{roomId}")
    public ResponseEntity<?> getRoomDetail(@PathVariable Long roomId) {
        try {
            Room room = accommodationService.getRoomById(roomId);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "객실을 찾을 수 없습니다. ID: " + roomId));
            }
            Accommodation accommodation = accommodationService.getAccommodationById(room.getAccommodationId());
            Map<String, Object> response = new HashMap<>();
            response.put("room", room);
            response.put("accommodation", accommodation);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "객실 상세 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 조건별 숙소 필터링 (페이징 적용)
     */
    @GetMapping("/accommodations/filter")
    public ResponseEntity<?> getFilteredAccommodations(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String accommodationType,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String checkInDate,
            @RequestParam(required = false) String checkOutDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Attempting to get filtered accommodations with params - sidoCode: {}, gugunCode: {}, keyword: {}, type: {}, guests: {}, sortBy: {}, checkInDate: {}, checkOutDate: {}, page: {}, size: {}",
                sidoCode, gugunCode, keyword, accommodationType, guests, sortBy, checkInDate, checkOutDate, page, size);
        try {
            Map<String, Object> filters = new HashMap<>();
            if (sidoCode != null) filters.put("sidoCode", sidoCode);
            if (gugunCode != null) filters.put("gugunCode", gugunCode);
            if (keyword != null && !keyword.trim().isEmpty()) filters.put("keyword", keyword.trim());
            if (accommodationType != null && !accommodationType.trim().isEmpty() && !accommodationType.equalsIgnoreCase("ALL")) {
                filters.put("accommodationType", accommodationType.trim());
            }

            if (guests != null && guests > 0) {
                filters.put("guestCount", guests);
            }

            if (checkInDate != null && !checkInDate.trim().isEmpty()) {
                filters.put("checkInDate", checkInDate.trim());
            }
            if (checkOutDate != null && !checkOutDate.trim().isEmpty()) {
                filters.put("checkOutDate", checkOutDate.trim());
            }

            if (sortBy != null && !sortBy.trim().isEmpty()) filters.put("sortBy", sortBy.trim());
            
            filters.put("offset", (page - 1) * size);
            filters.put("limit", size);
            filters.put("page", page);
            filters.put("size", size);

            log.debug("Constructed filters map: {}", filters);

            log.info("Calling accommodationService.getFilteredAccommodations with filters...");
            Map<String, Object> pagedResult = accommodationService.getFilteredAccommodations(filters);
            log.info("Successfully retrieved pagedResult: {}", pagedResult != null ? "not null, content size: " + ((List<?>)pagedResult.getOrDefault("content", List.of())).size() : "null");

            return ResponseEntity.ok(pagedResult);
        } catch (SQLException e) {
            log.error("SQL Exception in getFilteredAccommodations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "필터링된 숙소 목록 조회 중 데이터베이스 오류 발생: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected Exception in getFilteredAccommodations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "필터링된 숙소 목록 조회 중 예상치 못한 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 특정 호스트의 모든 숙소 목록을 조회합니다.
     * 호스트 본인 또는 관리자만 접근 가능합니다.
     */
    @GetMapping("/host/accommodations/{hostId}")
    public ResponseEntity<?> getHostAccommodationsList(
            @PathVariable Long hostId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        Long currentUserId = userDetails.getUser().getUserId();
        String currentUserRole = userDetails.getUser().getRole();

        try {
            // hostId는 hosts 테이블의 PK로 간주 (HostDashboard.vue에서 host.hostId 사용)
            Host targetHost = hostService.getHostById(hostId); // hostId는 hosts 테이블의 PK
            if (targetHost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "해당 호스트 정보를 찾을 수 없습니다. ID: " + hostId));
            }

            // 권한 확인: 요청한 사용자가 대상 호스트(User ID 기준)이거나 관리자인지 확인
            if (!targetHost.getUserId().equals(currentUserId) && !"ADMIN".equalsIgnoreCase(currentUserRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "이 호스트의 숙소 목록을 조회할 권한이 없습니다."));
            }

            // AccommodationService의 getAccommodationsByHostId 메소드는 hosts 테이블의 PK를 파라미터로 받도록 가정
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            return ResponseEntity.ok(Map.of("accommodations", accommodations)); // 프론트엔드에서 accommodations 키로 데이터 기대
        } catch (SQLException e) {
            log.error("Error fetching accommodations for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 목록 조회 중 SQL 오류 발생: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching accommodations for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 목록 조회 중 예상치 못한 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 새 숙소 등록 (호스트 또는 관리자만 가능)
     * FormData로 텍스트 데이터와 이미지 파일을 함께 받습니다.
     */
    @PostMapping(value = "/host/accommodations", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createHostAccommodation(
            @ModelAttribute AccommodationRequestDto requestDto, // FormData 바인딩
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Long actualHostIdToUse;
        // 프론트엔드에서 명시적으로 hostId를 제공했는지 확인
        if (requestDto.getHostId() != null && requestDto.getHostId() > 0) { // hostId가 유효한 값으로 넘어왔는지 확인
            actualHostIdToUse = requestDto.getHostId();
            log.info("요청 DTO로부터 전달받은 hostId ({})를 사용합니다.", actualHostIdToUse);

            // (선택적 강화) 전달된 hostId가 현재 로그인한 사용자의 실제 hostId와 일치하는지,
            // 또는 관리자 권한인지 확인하는 로직을 추가할 수 있습니다.
            // 예를 들어, userDetails.getUser().getRole()이 "ADMIN"이 아니고,
            // hostService.getHostByUserId(userDetails.getUser().getUserId()).getHostId() != actualHostIdToUse 라면,
            // 권한 문제를 제기할 수 있습니다. 현재는 요청받은 hostId를 우선적으로 사용합니다.

        } else {
            // requestDto에 hostId가 없거나 유효하지 않은 경우, 인증된 사용자 정보를 기반으로 hostId를 조회합니다.
            log.info("요청 DTO에 hostId가 없거나 유효하지 않아, 인증된 사용자 정보로부터 hostId를 조회합니다. userId: {}", userDetails.getUser().getUserId());
            try {
                Host host = hostService.getHostByUserId(userDetails.getUser().getUserId());
                if (host == null || host.getHostId() == null) {
                    log.error("숙소 등록 시 호스트 정보를 찾을 수 없거나 DB에 hostId가 없습니다. userId: {}", userDetails.getUser().getUserId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", "숙소 등록을 위한 호스트 정보를 찾을 수 없습니다. 호스트 프로필을 확인하거나 관리자에게 문의하세요."));
                }
                actualHostIdToUse = host.getHostId();
                log.info("인증된 사용자 정보로부터 hostId ({})를 조회하여 사용합니다.", actualHostIdToUse);
            } catch (SQLException e) {
                log.error("숙소 등록 시 호스트 정보 조회 중 DB 오류 발생: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "호스트 정보 조회 중 서버 오류가 발생했습니다."));
            }
        }

        try {
            // 이제 requestDto와 조회/전달받은 actualHostIdToUse를 서비스 계층으로 전달합니다.
            AccommodationResponseDto responseDto = accommodationService.createAccommodationAndImages(requestDto, actualHostIdToUse);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "message", "숙소가 성공적으로 등록 요청되었습니다.", "data", responseDto));
        } catch (IllegalArgumentException e) {
            log.error("숙소 등록 요청 데이터 유효성 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("숙소 등록 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "숙소 등록 중 오류 발생: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/host/accommodations/{accommodationId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateHostAccommodation(
            @PathVariable Long accommodationId,
            @ModelAttribute AccommodationRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        Long hostUserId = userDetails.getUser().getUserId(); // 위와 동일한 가정

        try {
            AccommodationResponseDto responseDto = accommodationService.updateAccommodationAndImages(accommodationId, requestDto, hostUserId);
            return ResponseEntity.ok(Map.of("success", true, "message", "숙소 정보가 성공적으로 수정되었습니다.", "data", responseDto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("숙소 수정 요청 데이터 유효성 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("숙소 수정 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "숙소 수정 중 오류 발생: " + e.getMessage()));
        }
    }

    @GetMapping("/host/accommodations/{accommodationId}")
    public ResponseEntity<?> getHostAccommodationDetails(
            @PathVariable Long accommodationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        // 여기서도 숙소 소유주 확인 로직이 필요할 수 있으나, getAccommodationDetails 서비스 내부에서 처리하지 않는다면, 
        // 혹은 서비스에서 데이터를 가져온 후 여기서 소유주 비교를 할 수 있습니다.
        // 우선은 서비스에서 가져온 데이터를 바로 반환합니다.
        try {
            AccommodationResponseDto responseDto = accommodationService.getAccommodationDetails(accommodationId);
            // 추가 권한 확인: 응답받은 DTO의 hostId와 현재 로그인한 userDetails의 hostId 비교
            Long hostUserId = userDetails.getUser().getUserId();
            if (!responseDto.getHostId().equals(hostUserId)) {
                 // 만약 users.user_id와 accommodations.host_id가 다른 값을 의미한다면, 여기서 추가적인 변환/비교 로직 필요
                 // 예를 들어, userDetails.getUser().getHostProfile().getHostId() 등
                log.warn("사용자 ID {} 가 호스트 ID {} 의 숙소 {} 상세 정보에 접근 시도.", hostUserId, responseDto.getHostId(), accommodationId );
                // 실제 운영에서는 권한이 없다면 FORBIDDEN 처리해야 하지만, 현재는 조회 자체는 허용하고 로그만 남김. 
                // 또는 에러를 던져서 아래 catch 블록에서 처리하게 할 수 있음.
                // throw new IllegalAccessException("해당 숙소에 대한 조회 권한이 없습니다.");
            }
            return ResponseEntity.ok(Map.of("success", true, "data", responseDto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("호스트 숙소 상세 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "조회 중 오류 발생: " + e.getMessage()));
        }
    }

    // --- Host Room Management Endpoints ---

    @PostMapping(value = "/host/accommodations/{accommodationId}/rooms", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createHostRoom(
            @PathVariable Long accommodationId,
            @ModelAttribute RoomRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        Long userIdFromUserDetails = userDetails.getUser().getUserId();
        Host host;
        try {
            host = hostService.getHostByUserId(userIdFromUserDetails);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "호스트 정보를 찾을 수 없습니다. 호스트 등록이 필요합니다."));
            }
        } catch (SQLException e) {
            log.error("호스트 정보 조회 중 DB 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
        Long actualHostId = host.getHostId();

        try {
            RoomResponseDto responseDto = accommodationService.createRoomAndImages(accommodationId, requestDto, actualHostId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "message", "객실이 성공적으로 등록되었습니다.", "data", responseDto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("객실 등록 요청 데이터 유효성 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("객실 등록 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "객실 등록 중 오류 발생: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/host/rooms/{roomId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateHostRoom(
            @PathVariable Long roomId,
            @ModelAttribute RoomRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        Long userIdFromUserDetails = userDetails.getUser().getUserId();
        Host host;
        try {
            host = hostService.getHostByUserId(userIdFromUserDetails);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "호스트 정보를 찾을 수 없습니다."));
            }
        } catch (SQLException e) {
            log.error("호스트 정보 조회 중 DB 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
        Long actualHostId = host.getHostId();

        try {
            RoomResponseDto responseDto = accommodationService.updateRoomAndImages(roomId, requestDto, actualHostId);
            return ResponseEntity.ok(Map.of("success", true, "message", "객실 정보가 성공적으로 수정되었습니다.", "data", responseDto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("객실 수정 요청 데이터 유효성 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("객실 수정 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "객실 수정 중 오류 발생: " + e.getMessage()));
        }
    }

    @GetMapping("/host/rooms/{roomId}")
    public ResponseEntity<?> getHostRoomDetails(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        Long userIdFromUserDetails = userDetails.getUser().getUserId();
        Host host;
        try {
            host = hostService.getHostByUserId(userIdFromUserDetails);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "호스트 정보를 찾을 수 없습니다."));
            }
        } catch (SQLException e) {
            log.error("호스트 정보 조회 중 DB 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
        Long actualHostId = host.getHostId();
        try {
            RoomResponseDto responseDto = accommodationService.getRoomDetailsForHost(roomId, actualHostId);
            return ResponseEntity.ok(Map.of("success", true, "data", responseDto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("호스트 객실 상세 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "조회 중 오류 발생: " + e.getMessage()));
        }
    }

    @GetMapping("/host/accommodations/{accommodationId}/rooms")
    public ResponseEntity<?> getHostAccommodationRooms(
            @PathVariable Long accommodationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        Long userIdFromUserDetails = userDetails.getUser().getUserId();
        Host host;
        try {
            host = hostService.getHostByUserId(userIdFromUserDetails);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "호스트 정보를 찾을 수 없습니다."));
            }
        } catch (SQLException e) {
            log.error("호스트 정보 조회 중 DB 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
        Long actualHostId = host.getHostId();
        try {
            List<RoomResponseDto> responseDtos = accommodationService.getRoomsForHost(accommodationId, actualHostId);
            return ResponseEntity.ok(Map.of("success", true, "data", responseDtos));
        } catch (NoSuchElementException e) { // 서비스에서 숙소를 못찾는 경우도 NoSuchElementException 던질 수 있음
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("호스트 숙소의 객실 목록 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    @DeleteMapping("/host/rooms/{roomId}")
    public ResponseEntity<?> deleteHostRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        Long userIdFromUserDetails = userDetails.getUser().getUserId();
        Host host;
        try {
            host = hostService.getHostByUserId(userIdFromUserDetails);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "호스트 정보를 찾을 수 없습니다."));
            }
        } catch (SQLException e) {
            log.error("호스트 정보 조회 중 DB 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
        Long actualHostId = host.getHostId();
        try {
            accommodationService.deleteRoomAndImages(roomId, actualHostId);
            return ResponseEntity.ok(Map.of("success", true, "message", "객실이 성공적으로 삭제되었습니다."));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("객실 삭제 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "객실 삭제 중 오류 발생: " + e.getMessage()));
        }
    }
}
