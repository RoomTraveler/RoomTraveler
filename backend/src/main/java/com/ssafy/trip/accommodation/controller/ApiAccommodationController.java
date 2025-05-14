package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.ApiAccommodationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * API를 통해 숙소 정보를 가져오는 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class ApiAccommodationController {

    private final ApiAccommodationService apiAccommodationService;

    /**
     * 모든 숙소 목록을 조회합니다.
     */
    @GetMapping("/list")
    public String listAccommodations(Model model) {
        try {
            List<Accommodation> accommodations = apiAccommodationService.getAllAccommodations();
            model.addAttribute("accommodations", accommodations);
            return "accommodation/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 지역별 숙소 목록을 조회합니다.
     */
    @GetMapping("/region")
    public String getAccommodationsByRegion(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode,
            Model model) {
        try {
            List<Accommodation> accommodations = apiAccommodationService.getAccommodationsByRegion(sidoCode, gugunCode);
            model.addAttribute("accommodations", accommodations);
            model.addAttribute("sidoCode", sidoCode);
            model.addAttribute("gugunCode", gugunCode);
            return "accommodation/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 키워드로 숙소를 검색합니다.
     */
    @GetMapping("/search")
    public String searchAccommodations(
            @RequestParam String keyword,
            Model model) {
        try {
            List<Accommodation> accommodations = apiAccommodationService.searchAccommodations(keyword);
            model.addAttribute("accommodations", accommodations);
            model.addAttribute("keyword", keyword);
            return "accommodation/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 숙소 상세 정보를 조회합니다.
     */
    @GetMapping("/detail/{accommodationId}")
    public String getAccommodationDetail(
            @PathVariable Long accommodationId,
            Model model) {
        try {
            Accommodation accommodation = apiAccommodationService.getAccommodationById(accommodationId);
            List<Room> rooms = apiAccommodationService.getRoomsByAccommodationId(accommodationId);
            
            model.addAttribute("accommodation", accommodation);
            model.addAttribute("rooms", rooms);
            return "accommodation/detail";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 객실 상세 정보를 조회합니다.
     */
    @GetMapping("/room-detail/{roomId}")
    public String getRoomDetail(
            @PathVariable Long roomId,
            Model model) {
        try {
            Room room = apiAccommodationService.getRoomById(roomId);
            if (room == null) {
                model.addAttribute("error", "객실을 찾을 수 없습니다.");
                return "error";
            }
            
            Accommodation accommodation = apiAccommodationService.getAccommodationById(room.getAccommodationId());
            
            model.addAttribute("room", room);
            model.addAttribute("accommodation", accommodation);
            return "accommodation/room-detail";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 필터링된 숙소 목록을 조회합니다.
     */
    @GetMapping("/filter")
    public String getFilteredAccommodations(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String accommodationType,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sortBy,
            Model model) {
        try {
            Map<String, Object> filters = new HashMap<>();
            if (sidoCode != null) filters.put("sidoCode", sidoCode);
            if (gugunCode != null) filters.put("gugunCode", gugunCode);
            if (keyword != null && !keyword.isEmpty()) filters.put("keyword", keyword);
            if (accommodationType != null && !accommodationType.isEmpty()) filters.put("accommodationType", accommodationType);
            if (minPrice != null) filters.put("minPrice", minPrice);
            if (maxPrice != null) filters.put("maxPrice", maxPrice);
            if (sortBy != null && !sortBy.isEmpty()) filters.put("sortBy", sortBy);
            
            List<Accommodation> accommodations = apiAccommodationService.getFilteredAccommodations(filters);
            
            model.addAttribute("accommodations", accommodations);
            model.addAttribute("filters", filters);
            return "accommodation/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}