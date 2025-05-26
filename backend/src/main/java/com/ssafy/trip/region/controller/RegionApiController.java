package com.ssafy.trip.region.controller;

import com.ssafy.trip.common.BaseException;
import com.ssafy.trip.region.model.Gugun;
import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 지역 정보 REST API 컨트롤러
 * 
 * 시도 및 구군 정보를 제공하는 REST API 엔드포인트를 정의합니다.
 * 
 * @author AI Assistant
 */
@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Region", description = "지역(시도/구군) 정보 API")
public class RegionApiController {

    private final RegionService regionService;

    @Operation(summary = "모든 시도 목록 조회", description = "등록된 모든 시도 정보를 조회합니다.")
    @GetMapping(value = "/sidos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllSidos() {
        try {
            List<Sido> sidos = regionService.getAllSidos();
            if (sidos == null || sidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(sidos);
        } catch (Exception e) {
            log.error("시도 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "시도 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    @Operation(summary = "특정 시도 정보 조회", description = "시도 코드를 기준으로 특정 시도 정보를 조회합니다.")
    @GetMapping("/sidos/{sidoCode}")
    public ResponseEntity<?> getSidoByCode(@Parameter(description = "시도 코드", required = true) @PathVariable int sidoCode) {
        try {
            Sido sido = regionService.getSidoByCode(sidoCode);
            if (sido == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(sidoCode + "에 해당하는 시도 정보가 없습니다.");
            }
            return ResponseEntity.ok(sido);
        } catch (Exception e) {
            log.error("시도 정보 조회 중 오류 발생 (sidoCode: {}):", sidoCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시도 정보 조회 중 오류 발생");
        }
    }
    
    @Operation(summary = "[관리자] 새로운 시도 정보 등록", description = "새로운 시도 정보를 시스템에 등록합니다.")
    @PostMapping("/sidos")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 관리자 권한 필요 시 추가
    public ResponseEntity<?> addSido(@Valid @RequestBody Sido sido) {
        try {
            regionService.addSido(sido);
            return ResponseEntity.status(HttpStatus.CREATED).body(sido);
        } catch (BaseException e) {
            log.warn("시도 정보 등록 실패 (비즈니스 로직 오류): {} - {}", e.getErrorCode().getCode(), e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getMessage());
        } catch (Exception e) {
            log.error("시도 정보 등록 중 오류 발생: {}", sido, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시도 정보 등록 중 오류 발생: " + e.getMessage());
        }
    }

    @Operation(summary = "[관리자] 시도 정보 수정", description = "기존 시도 정보를 수정합니다. sidoCode는 변경할 수 없습니다.")
    @PutMapping("/sidos/{sidoCode}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modifySido(
            @Parameter(description = "수정할 시도의 코드", required = true) @PathVariable int sidoCode,
            @Valid @RequestBody Sido sidoUpdateDto
    ) {
        try {
            Sido existingSido = regionService.getSidoByCode(sidoCode);
            if (existingSido == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(sidoCode + "에 해당하는 시도 정보가 없습니다.");
            }
            sidoUpdateDto.setCode(sidoCode);
            regionService.modifySido(sidoUpdateDto);
            return ResponseEntity.ok(sidoUpdateDto);
        } catch (BaseException e) {
            log.warn("시도 정보 수정 실패 (비즈니스 로직 오류): {} - {}", e.getErrorCode().getCode(), e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getMessage());
        } catch (Exception e) {
            log.error("시도 정보 수정 중 오류 발생 (sidoCode: {}): {}", sidoCode, sidoUpdateDto, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시도 정보 수정 중 오류 발생: " + e.getMessage());
        }
    }

    @Operation(summary = "[관리자] 시도 정보 삭제", description = "시도 코드를 기준으로 특정 시도 정보를 삭제합니다.")
    @DeleteMapping("/sidos/{sidoCode}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeSido(@Parameter(description = "삭제할 시도 코드", required = true) @PathVariable int sidoCode) {
        try {
            Sido existingSido = regionService.getSidoByCode(sidoCode);
            if (existingSido == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(sidoCode + "에 해당하는 시도 정보가 없습니다. 이미 삭제되었거나 존재하지 않을 수 있습니다.");
            }
            regionService.removeSido(sidoCode);
            return ResponseEntity.ok().body(sidoCode + " 시도 정보가 성공적으로 삭제되었습니다.");
        } catch (BaseException e) {
             log.warn("시도 정보 삭제 실패 (비즈니스 로직 오류): {} - {}", e.getErrorCode().getCode(), e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getMessage());
        } catch (Exception e) {
            log.error("시도 정보 삭제 중 오류 발생 (sidoCode: {}):", sidoCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시도 정보 삭제 중 오류 발생 (관련 구군 정보가 있을 수 있습니다): " + e.getMessage());
        }
    }

    @Operation(summary = "특정 시도의 구군 목록 조회", description = "시도 코드를 기준으로 해당 시도에 속한 모든 구군 정보를 조회합니다.")
    @GetMapping(value = "/guguns", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGugunsBySidoCode(@Parameter(description = "시도 코드", required = true) @RequestParam("sidoCode") int sidoCode) {
        try {
            List<Gugun> guguns = regionService.getGugunsBySidoCode(sidoCode);
             if (guguns == null || guguns.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(guguns);
        } catch (NumberFormatException e) {
            log.error("잘못된 시도 코드 형식: {}", sidoCode, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "잘못된 시도 코드 형식입니다."));
        } catch (Exception e) {
            log.error("구군 목록 조회 중 오류 발생 (sidoCode: {}):", sidoCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "구군 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    @Operation(summary = "[관리자] 모든 시도 이미지 URL 일괄 업데이트", description = "미리 정의된 매핑에 따라 모든 시도의 이미지 URL을 일괄적으로 업데이트합니다.")
    @PostMapping("/sidos/admin/update-image-urls")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAllSidoImageUrls() {
        try {
            regionService.updateSidoImageUrls();
            return ResponseEntity.ok(Map.of("message", "모든 시도 이미지 URL이 성공적으로 업데이트되었습니다."));
        } catch (Exception e) {
            log.error("시도 이미지 URL 일괄 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "시도 이미지 URL 일괄 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }
}
