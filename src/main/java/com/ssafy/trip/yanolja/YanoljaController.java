package com.ssafy.trip.yanolja;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ssafy.trip.accommodation.service.AccommodationService;

import lombok.RequiredArgsConstructor;

/**
 * 야놀자 클론 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/yanolja")
public class YanoljaController {
    
    private final AccommodationService accommodationService;
    
    /**
     * 야놀자 메인 페이지로 이동합니다.
     */
    @GetMapping({"", "/"})
    public String index(Model model) {
        try {
            // 특가 숙소 목록 (실제 구현에서는 특가 정보가 있는 숙소만 조회)
            model.addAttribute("specialOffers", accommodationService.getAllAccommodations());
            
            // 추천 숙소 목록 (실제 구현에서는 평점이 높은 숙소만 조회)
            model.addAttribute("recommendedAccommodations", accommodationService.getAllAccommodations());
            
            return "yanolja/index";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 야놀자 스타일의 숙소 목록 페이지로 이동합니다.
     */
    @GetMapping("/accommodations")
    public String accommodationList(Model model) {
        try {
            model.addAttribute("accommodations", accommodationService.getAllAccommodations());
            return "yanolja/accommodation-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 야놀자 스타일의 숙소 상세 페이지로 이동합니다.
     */
    @GetMapping("/accommodation/{id}")
    public String accommodationDetail(Long id, Model model) {
        try {
            model.addAttribute("accommodation", accommodationService.getAccommodationById(id));
            model.addAttribute("rooms", accommodationService.getRoomsByAccommodationId(id));
            return "yanolja/accommodation-detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 야놀자 스타일의 객실 상세 페이지로 이동합니다.
     */
    @GetMapping("/room/{id}")
    public String roomDetail(Long id, Model model) {
        try {
            model.addAttribute("room", accommodationService.getRoomById(id));
            return "yanolja/room-detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 야놀자 스타일의 검색 결과 페이지로 이동합니다.
     */
    @GetMapping("/search")
    public String search(String keyword, Model model) {
        try {
            model.addAttribute("keyword", keyword);
            model.addAttribute("accommodations", accommodationService.searchAccommodations(keyword));
            return "yanolja/search-results";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}