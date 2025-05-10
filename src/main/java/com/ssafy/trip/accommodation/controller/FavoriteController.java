package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Favorite;
import com.ssafy.trip.accommodation.service.FavoriteService;

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
 * 즐겨찾기(찜) 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 즐겨찾기 목록 페이지로 이동합니다.
     */
    @GetMapping("/favorites")
    public String favorites(HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            List<Favorite> favorites = favoriteService.getFavoritesByUserId(userId);
            model.addAttribute("favorites", favorites);
            return "accommodation/favorites";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 즐겨찾기에 숙소를 추가합니다.
     */
    @PostMapping("/add-favorite")
    @ResponseBody
    public Map<String, Object> addFavorite(
            @RequestParam Long accommodationId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            Long favoriteId = favoriteService.addFavorite(userId, accommodationId);
            response.put("success", true);
            response.put("favoriteId", favoriteId);
            response.put("message", "즐겨찾기에 추가되었습니다.");
            return response;
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "즐겨찾기 추가 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    /**
     * 즐겨찾기에서 숙소를 삭제합니다.
     */
    @PostMapping("/remove-favorite")
    @ResponseBody
    public Map<String, Object> removeFavorite(
            @RequestParam Long favoriteId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            boolean removed = favoriteService.removeFavorite(favoriteId, userId);
            if (removed) {
                response.put("success", true);
                response.put("message", "즐겨찾기에서 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "즐겨찾기 삭제에 실패했습니다.");
            }
            return response;
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "즐겨찾기 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    /**
     * 즐겨찾기 목록에서 숙소를 삭제합니다. (페이지에서 직접 삭제)
     */
    @GetMapping("/remove-favorite/{favoriteId}")
    public String removeFavoriteFromList(
            @PathVariable Long favoriteId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            boolean removed = favoriteService.removeFavorite(favoriteId, userId);
            if (removed) {
                redirectAttributes.addFlashAttribute("message", "즐겨찾기에서 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "즐겨찾기 삭제에 실패했습니다.");
            }
            return "redirect:/accommodation/favorites";
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "즐겨찾기 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/accommodation/favorites";
        }
    }

    /**
     * 사용자가 특정 숙소를 즐겨찾기에 추가했는지 확인합니다.
     */
    @GetMapping("/check-favorite")
    @ResponseBody
    public Map<String, Object> checkFavorite(
            @RequestParam Long accommodationId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.put("isFavorite", false);
            return response;
        }

        try {
            boolean isFavorite = favoriteService.isFavorite(userId, accommodationId);
            response.put("isFavorite", isFavorite);
            
            if (isFavorite) {
                Favorite favorite = favoriteService.getFavoritesByUserId(userId).stream()
                        .filter(f -> f.getAccommodationId().equals(accommodationId))
                        .findFirst()
                        .orElse(null);
                
                if (favorite != null) {
                    response.put("favoriteId", favorite.getFavoriteId());
                }
            }
            
            return response;
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("isFavorite", false);
            response.put("error", e.getMessage());
            return response;
        }
    }
}