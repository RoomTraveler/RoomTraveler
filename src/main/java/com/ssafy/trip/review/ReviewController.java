package com.ssafy.trip.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 컨트롤러
 * 리뷰 관련 웹 요청을 처리합니다.
 */
@Controller
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     */
    @GetMapping("/accommodation/{accommodationId}")
    public String getReviewsByAccommodationId(
            @PathVariable Long accommodationId,
            Model model) {
        try {
            List<Review> reviews = reviewService.getReviewsByAccommodationId(accommodationId);
            Double averageRating = reviewService.getAverageRatingByAccommodationId(accommodationId);
            Integer reviewCount = reviewService.getReviewCountByAccommodationId(accommodationId);
            
            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("reviewCount", reviewCount);
            model.addAttribute("accommodationId", accommodationId);
            
            return "review/review-list";
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 리뷰 작성 폼을 표시합니다.
     */
    @GetMapping("/write/{accommodationId}")
    public String showReviewForm(
            @PathVariable Long accommodationId,
            @SessionAttribute(name = "userId", required = false) Long userId,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            redirectAttributes.addFlashAttribute("alertMsg", "리뷰를 작성하려면 로그인이 필요합니다.");
            return "redirect:/user/login-form";
        }
        
        try {
            // 리뷰 작성 자격 확인
            boolean isEligible = reviewService.checkReviewEligibility(userId, accommodationId);
            
            if (!isEligible) {
                redirectAttributes.addFlashAttribute("alertMsg", "해당 숙소에 숙박한 기록이 없어 리뷰를 작성할 수 없습니다.");
                return "redirect:/accommodation/detail?accommodationId=" + accommodationId;
            }
            
            model.addAttribute("accommodationId", accommodationId);
            model.addAttribute("review", new Review());
            
            return "review/review-form";
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "리뷰 작성 폼을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 리뷰를 저장합니다.
     */
    @PostMapping("/write")
    public String saveReview(
            @ModelAttribute Review review,
            @SessionAttribute(name = "userId", required = false) Long userId,
            RedirectAttributes redirectAttributes) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            redirectAttributes.addFlashAttribute("alertMsg", "리뷰를 작성하려면 로그인이 필요합니다.");
            return "redirect:/user/login-form";
        }
        
        try {
            // 사용자 ID 설정
            review.setUserId(userId);
            
            // 기본값 설정
            review.setCreatedAt(LocalDateTime.now());
            review.setStatus("ACTIVE");
            review.setIsVerified(true); // 실제로는 예약 시스템과 연동하여 확인해야 함
            
            // 리뷰 저장
            Long reviewId = reviewService.createReview(review);
            
            redirectAttributes.addFlashAttribute("alertMsg", "리뷰가 성공적으로 등록되었습니다.");
            return "redirect:/accommodation/detail?accommodationId=" + review.getAccommodationId();
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 저장 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/review/write/" + review.getAccommodationId();
        }
    }

    /**
     * 리뷰 수정 폼을 표시합니다.
     */
    @GetMapping("/edit/{reviewId}")
    public String showEditForm(
            @PathVariable Long reviewId,
            @SessionAttribute(name = "userId", required = false) Long userId,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            redirectAttributes.addFlashAttribute("alertMsg", "리뷰를 수정하려면 로그인이 필요합니다.");
            return "redirect:/user/login-form";
        }
        
        try {
            Review review = reviewService.getReviewById(reviewId);
            
            // 리뷰가 존재하지 않는 경우
            if (review == null) {
                redirectAttributes.addFlashAttribute("alertMsg", "존재하지 않는 리뷰입니다.");
                return "redirect:/";
            }
            
            // 리뷰 작성자가 아닌 경우
            if (!review.getUserId().equals(userId)) {
                redirectAttributes.addFlashAttribute("alertMsg", "자신이 작성한 리뷰만 수정할 수 있습니다.");
                return "redirect:/accommodation/detail?accommodationId=" + review.getAccommodationId();
            }
            
            model.addAttribute("review", review);
            
            return "review/review-edit-form";
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "리뷰 수정 폼을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 리뷰를 업데이트합니다.
     */
    @PostMapping("/edit")
    public String updateReview(
            @ModelAttribute Review review,
            @SessionAttribute(name = "userId", required = false) Long userId,
            RedirectAttributes redirectAttributes) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            redirectAttributes.addFlashAttribute("alertMsg", "리뷰를 수정하려면 로그인이 필요합니다.");
            return "redirect:/user/login-form";
        }
        
        try {
            Review existingReview = reviewService.getReviewById(review.getReviewId());
            
            // 리뷰가 존재하지 않는 경우
            if (existingReview == null) {
                redirectAttributes.addFlashAttribute("alertMsg", "존재하지 않는 리뷰입니다.");
                return "redirect:/";
            }
            
            // 리뷰 작성자가 아닌 경우
            if (!existingReview.getUserId().equals(userId)) {
                redirectAttributes.addFlashAttribute("alertMsg", "자신이 작성한 리뷰만 수정할 수 있습니다.");
                return "redirect:/accommodation/detail?accommodationId=" + existingReview.getAccommodationId();
            }
            
            // 기존 리뷰의 변경하지 않을 필드 유지
            review.setUserId(existingReview.getUserId());
            review.setAccommodationId(existingReview.getAccommodationId());
            review.setCreatedAt(existingReview.getCreatedAt());
            review.setIsVerified(existingReview.getIsVerified());
            review.setStatus(existingReview.getStatus());
            
            // 리뷰 업데이트
            boolean updated = reviewService.updateReview(review);
            
            if (updated) {
                redirectAttributes.addFlashAttribute("alertMsg", "리뷰가 성공적으로 수정되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("alertMsg", "리뷰 수정에 실패했습니다.");
            }
            
            return "redirect:/accommodation/detail?accommodationId=" + review.getAccommodationId();
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/review/edit/" + review.getReviewId();
        }
    }

    /**
     * 리뷰를 삭제합니다.
     */
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            @SessionAttribute(name = "userId", required = false) Long userId,
            @SessionAttribute(name = "role", required = false) String role,
            RedirectAttributes redirectAttributes) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            redirectAttributes.addFlashAttribute("alertMsg", "리뷰를 삭제하려면 로그인이 필요합니다.");
            return "redirect:/user/login-form";
        }
        
        try {
            Review review = reviewService.getReviewById(reviewId);
            
            // 리뷰가 존재하지 않는 경우
            if (review == null) {
                redirectAttributes.addFlashAttribute("alertMsg", "존재하지 않는 리뷰입니다.");
                return "redirect:/";
            }
            
            // 리뷰 작성자가 아니고 관리자도 아닌 경우
            if (!review.getUserId().equals(userId) && !"ADMIN".equals(role)) {
                redirectAttributes.addFlashAttribute("alertMsg", "자신이 작성한 리뷰만 삭제할 수 있습니다.");
                return "redirect:/accommodation/detail?accommodationId=" + review.getAccommodationId();
            }
            
            // 리뷰 삭제
            boolean deleted = reviewService.deleteReview(reviewId);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("alertMsg", "리뷰가 성공적으로 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("alertMsg", "리뷰 삭제에 실패했습니다.");
            }
            
            return "redirect:/accommodation/detail?accommodationId=" + review.getAccommodationId();
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     */
    @GetMapping("/user")
    public String getReviewsByUser(
            @SessionAttribute(name = "userId", required = false) Long userId,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            redirectAttributes.addFlashAttribute("alertMsg", "내 리뷰를 보려면 로그인이 필요합니다.");
            return "redirect:/user/login-form";
        }
        
        try {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            model.addAttribute("reviews", reviews);
            
            return "review/my-reviews";
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }
}