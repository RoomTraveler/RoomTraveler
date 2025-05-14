package com.ssafy.trip.accommodation;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.service.AccommodationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AccommodationIndexController {

    private final AccommodationService accommodationService;

    @GetMapping("/accommodation")
    public String showPage(
            @RequestParam(value = "sidoCode", required = false) Integer sidoCode,
            @RequestParam(value = "gugunCode", required = false) Integer gugunCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Model model) {

        try {
            List<Accommodation> accommodations;

            // 필터링 조건이 있는 경우
            if (sidoCode != null || (keyword != null && !keyword.isEmpty()) || minPrice != null || maxPrice != null || sortBy != null) {
                Map<String, Object> filters = new HashMap<>();

                if (sidoCode != null) filters.put("sidoCode", sidoCode);
                if (gugunCode != null) filters.put("gugunCode", gugunCode);
                if (keyword != null && !keyword.isEmpty()) filters.put("keyword", keyword);
                if (minPrice != null) filters.put("minPrice", minPrice);
                if (maxPrice != null) filters.put("maxPrice", maxPrice);
                if (sortBy != null) filters.put("sortBy", sortBy);
                if (sortOrder != null) filters.put("sortOrder", sortOrder);

                // 활성 상태인 숙소만 조회
                filters.put("status", "ACTIVE");

                accommodations = accommodationService.getFilteredAccommodations(filters);

                // 검색 조건을 모델에 추가
                model.addAttribute("sidoCode", sidoCode);
                model.addAttribute("gugunCode", gugunCode);
                model.addAttribute("keyword", keyword);
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);
                model.addAttribute("sortBy", sortBy);
                model.addAttribute("sortOrder", sortOrder);
            } else {
                // 모든 숙소 조회
                accommodations = accommodationService.getAllAccommodations();
            }

            model.addAttribute("accommodations", accommodations);
            return "accommodation/index";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
