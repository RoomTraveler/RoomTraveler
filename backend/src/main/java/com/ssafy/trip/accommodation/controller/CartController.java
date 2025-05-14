package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Cart;
import com.ssafy.trip.accommodation.model.CartItem;
import com.ssafy.trip.accommodation.service.CartService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 장바구니 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 페이지로 이동합니다.
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Cart cart = cartService.getOrCreateCart(userId);
            model.addAttribute("cart", cart);
            return "accommodation/cart";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 장바구니에 객실을 추가합니다.
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam Integer guestCount,
            @RequestParam Double price,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            cartService.addToCart(userId, roomId, checkInDate, checkOutDate, guestCount, price);
            redirectAttributes.addFlashAttribute("message", "객실이 장바구니에 추가되었습니다.");
            return "redirect:/cart";
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accommodation/room-detail?roomId=" + roomId;
        }
    }

    /**
     * 장바구니에서 객실을 삭제합니다.
     */
    @GetMapping("/remove/{cartItemId}")
    public String removeFromCart(
            @PathVariable Long cartItemId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            boolean removed = cartService.removeFromCart(cartItemId, userId);
            if (removed) {
                redirectAttributes.addFlashAttribute("message", "객실이 장바구니에서 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "객실 삭제에 실패했습니다.");
            }
            return "redirect:/cart";
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * 장바구니를 비웁니다.
     */
    @GetMapping("/clear")
    public String clearCart(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            int count = cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("message", count + "개의 객실이 장바구니에서 삭제되었습니다.");
            return "redirect:/cart";
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * 장바구니에서 예약 페이지로 이동합니다.
     */
    @PostMapping("/checkout")
    public String checkout(
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Cart cart = cartService.getOrCreateCart(userId);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "장바구니가 비어있습니다.");
                return "redirect:/cart";
            }

            // 장바구니 아이템을 세션에 저장하여 예약 페이지로 전달
            session.setAttribute("cartItems", cart.getItems());
            session.setAttribute("totalPrice", cart.getTotalPrice());
            
            return "redirect:/reservation/cart-checkout";
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}