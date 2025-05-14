package com.ssafy.trip.host;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.review.Review;
import com.ssafy.trip.review.ReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 호스트 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/host")
public class HostController {
    private final HostService hostService;
    private final UserService userService;
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    /**
     * 호스트 메인 페이지로 이동합니다.
     */
    @GetMapping({"", "/index"})
    public String index() {
        return "host/index";
    }

    /**
     * 호스트 로그인 폼 페이지로 이동합니다.
     */
    @GetMapping("/login-form")
    public String loginForm() {
        return "host/login-form";
    }

    /**
     * 호스트 회원가입 폼 페이지로 이동합니다.
     */
    @GetMapping("/regist-user-form")
    public String registUserForm() {
        return "host/regist-user-form";
    }

    /**
     * 호스트 로그인 처리를 합니다.
     */
    @PostMapping("/login")
    public String login(@ModelAttribute User user, HttpSession session, RedirectAttributes redir, Model model) {
        try {
            // 이메일과 비밀번호로 사용자 조회
            User loginUser = userService.login(user.getEmail(), user.getPassword());

            // 사용자가 존재하지 않거나 사용자명이 없는 경우
            if (loginUser == null || loginUser.getUsername() == null) {
                throw new Exception("잘못된 입력!");
            }

            // 호스트 또는 관리자가 아닌 경우
            if (!"HOST".equals(loginUser.getRole()) && !"ADMIN".equals(loginUser.getRole())) {
                model.addAttribute("error", "호스트 또는 관리자만 접근할 수 있습니다.");
                return "host/login-form";
            }

            // 로그인 성공 시 세션에 사용자 정보 저장 (비밀번호 제외)
            session.setAttribute("userId", loginUser.getUserId());
            session.setAttribute("username", loginUser.getUsername());
            session.setAttribute("email", loginUser.getEmail());
            // 사용자 역할 정보도 세션에 저장
            session.setAttribute("role", loginUser.getRole());
            redir.addFlashAttribute("alertMsg", "호스트 로그인에 성공했습니다!");
            return "redirect:/host";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "host/login-form";
        }
    }

    /**
     * 새 호스트 사용자를 등록합니다.
     */
    @PostMapping("/regist-user")
    public String registUser(@ModelAttribute User user, RedirectAttributes redir, Model model) {
        // 역할을 HOST로 설정
        user.setRole("HOST");

        try {
            userService.registUser(user);
            redir.addFlashAttribute("alertMsg", "호스트로 등록되었습니다. 로그인 후 사용해주세요.");
            return "redirect:/host/login-form";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "host/regist-user-form";
        }
    }

    /**
     * 호스트 등록 폼 페이지로 이동합니다.
     */
    @GetMapping("/regist-form")
    public String registForm(HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        return "host/regist-form";
    }

    /**
     * 호스트 상세 페이지로 이동합니다.
     */
    @GetMapping("/detail/{hostId}")
    public String hostDetail(@PathVariable Long hostId, Model model) {
        try {
            Host host = hostService.getHostById(hostId);
            model.addAttribute("host", host);
            return "host/detail";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 호스트 목록 페이지로 이동합니다.
     */
    @GetMapping("/list")
    public String hostList(Model model) {
        try {
            List<Host> hosts = hostService.getHostList();
            model.addAttribute("hosts", hosts);
            return "host/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 새 호스트를 등록합니다.
     */
    @PostMapping("/regist")
    public String registHost(@ModelAttribute Host host, HttpSession session, RedirectAttributes redir, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            // 호스트 ID는 사용자 ID와 동일
            host.setHostId(userId);
            // 기본 상태는 PENDING
            host.setHostStatus("PENDING");

            hostService.registHost(host);

            // 사용자 권한을 HOST로 변경
            userService.updateUserRole(userId, "HOST");

            redir.addFlashAttribute("alertMsg", "호스트 등록 신청이 완료되었습니다. 승인을 기다려주세요.");
            return "redirect:/";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "host/regist-form";
        }
    }

    /**
     * 호스트 정보를 업데이트합니다.
     */
    @PostMapping("/update")
    public String updateHost(@ModelAttribute Host host, HttpSession session, RedirectAttributes redir, Model model) {
        // 로그인 확인 및 권한 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !userId.equals(host.getHostId())) {
            return "redirect:/user/login-form";
        }

        try {
            hostService.updateHost(host);
            redir.addFlashAttribute("alertMsg", "호스트 정보가 업데이트되었습니다.");
            return "redirect:/host/detail/" + host.getHostId();
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "host/update-form";
        }
    }

    /**
     * 호스트 상태를 업데이트합니다. (관리자 전용)
     */
    @PostMapping("/update-status/{hostId}")
    public String updateHostStatus(@PathVariable Long hostId, String hostStatus, HttpSession session, RedirectAttributes redir, Model model) {
        // 관리자 권한 확인
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            redir.addFlashAttribute("alertMsg", "관리자만 접근할 수 있습니다.");
            return "redirect:/";
        }

        try {
            hostService.updateHostStatus(hostId, hostStatus);
            redir.addFlashAttribute("alertMsg", "호스트 상태가 업데이트되었습니다.");
            return "redirect:/host/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "redirect:/host/list";
        }
    }

    /**
     * 숙소 등록 페이지로 이동합니다. (승인된 호스트 또는 관리자만 가능)
     */
    @GetMapping("/register-accommodation")
    public String registerAccommodation(HttpSession session, RedirectAttributes redir, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        // 사용자 역할 확인
        String role = (String) session.getAttribute("role");

        // 관리자인 경우 바로 숙소 등록 페이지로 이동
        if ("ADMIN".equals(role)) {
            return "redirect:/accommodation/register-form";
        }

        // 호스트인지 확인
        if (!"HOST".equals(role)) {
            redir.addFlashAttribute("alertMsg", "호스트만 숙소를 등록할 수 있습니다.");
            return "redirect:/";
        }

        try {
            // 호스트 정보 조회
            Host host = hostService.getHostById(userId);

            // 승인된 호스트인지 확인
            if (host == null || !"APPROVED".equals(host.getHostStatus())) {
                redir.addFlashAttribute("alertMsg", "승인된 호스트만 숙소를 등록할 수 있습니다.");
                return "redirect:/";
            }

            return "redirect:/accommodation/register-form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 호스트 예약 관리 페이지로 이동합니다.
     */
    @GetMapping("/reservations")
    public String hostReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long accommodationId,
            HttpSession session, 
            Model model) {

        // 로그인 및 호스트/관리자 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            // 호스트의 숙소 목록 조회
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            model.addAttribute("accommodations", accommodations);

            // 호스트의 예약 목록 조회
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId);

            // 필터링 적용
            if (status != null && !status.isEmpty()) {
                List<Reservation> filteredReservations = new ArrayList<>();
                for (Reservation reservation : reservations) {
                    if (status.equals(reservation.getStatus())) {
                        filteredReservations.add(reservation);
                    }
                }
                reservations = filteredReservations;
            }

            if (accommodationId != null) {
                List<Reservation> filteredReservations = new ArrayList<>();
                for (Reservation reservation : reservations) {
                    try {
                        Room room = accommodationService.getRoomById(reservation.getRoomId());
                        if (accommodationId.equals(room.getAccommodationId())) {
                            filteredReservations.add(reservation);
                        }
                    } catch (SQLException e) {
                        // 객실 정보를 가져오지 못한 경우 무시
                    }
                }
                reservations = filteredReservations;
            }

            model.addAttribute("reservations", reservations);

            return "host/host-reservations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 호스트 리뷰 관리 페이지로 이동합니다.
     */
    @GetMapping("/reviews")
    public String hostReviews(
            @RequestParam(required = false) Long accommodationId,
            @RequestParam(required = false) Integer rating,
            HttpSession session, 
            Model model) {

        // 로그인 및 호스트/관리자 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            // 호스트의 숙소 목록 조회
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            model.addAttribute("accommodations", accommodations);

            // 호스트의 모든 숙소에 대한 리뷰 조회
            List<Review> reviews = new ArrayList<>();
            int totalReviews = 0;
            double totalRating = 0;

            for (Accommodation accommodation : accommodations) {
                List<Review> accommodationReviews = reviewService.getReviewsByAccommodationId(accommodation.getAccommodationId());

                // 필터링 적용
                if (rating != null) {
                    List<Review> filteredReviews = new ArrayList<>();
                    for (Review review : accommodationReviews) {
                        if (rating.equals(review.getRating())) {
                            filteredReviews.add(review);
                        }
                    }
                    accommodationReviews = filteredReviews;
                }

                // 통계 계산
                totalReviews += accommodationReviews.size();
                for (Review review : accommodationReviews) {
                    if (review.getRating() != null) {
                        totalRating += review.getRating();
                    }
                }

                reviews.addAll(accommodationReviews);
            }

            // 특정 숙소에 대한 필터링
            if (accommodationId != null) {
                List<Review> filteredReviews = new ArrayList<>();
                for (Review review : reviews) {
                    if (accommodationId.equals(review.getAccommodationId())) {
                        filteredReviews.add(review);
                    }
                }
                reviews = filteredReviews;
            }

            // 통계 데이터 설정
            model.addAttribute("reviews", reviews);
            model.addAttribute("totalReviews", totalReviews);
            model.addAttribute("averageRating", totalReviews > 0 ? Math.round((totalRating / totalReviews) * 10.0) / 10.0 : 0);

            return "host/host-reviews";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 호스트 숙소 관리 페이지로 이동합니다.
     */
    @GetMapping("/accommodations")
    public String hostAccommodations(HttpSession session, Model model) {
        // 로그인 및 호스트/관리자 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            // 호스트의 숙소 목록 조회
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            model.addAttribute("accommodations", accommodations);

            // 각 숙소별 객실 목록을 맵으로 저장
            java.util.Map<Long, List<Room>> roomsByAccommodation = new java.util.HashMap<>();

            // 통계 데이터 계산
            int totalRooms = 0;
            int activeAccommodations = 0;
            double totalRating = 0;
            int ratedAccommodations = 0;

            for (Accommodation accommodation : accommodations) {
                // 객실 목록 조회 및 저장
                List<Room> rooms = accommodationService.getRoomsByAccommodationId(accommodation.getAccommodationId());
                roomsByAccommodation.put(accommodation.getAccommodationId(), rooms);

                // 객실 수 계산
                totalRooms += rooms.size();

                // 활성 숙소 수 계산
                if ("ACTIVE".equals(accommodation.getStatus())) {
                    activeAccommodations++;
                }

                // 평균 평점 계산
                Double rating = reviewService.getAverageRatingByAccommodationId(accommodation.getAccommodationId());
                if (rating != null && rating > 0) {
                    totalRating += rating;
                    ratedAccommodations++;
                }
            }

            model.addAttribute("roomsByAccommodation", roomsByAccommodation);
            model.addAttribute("totalRooms", totalRooms);
            model.addAttribute("activeAccommodations", activeAccommodations);
            model.addAttribute("averageRating", ratedAccommodations > 0 ? Math.round((totalRating / ratedAccommodations) * 10.0) / 10.0 : 0);

            return "host/host-accommodations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
