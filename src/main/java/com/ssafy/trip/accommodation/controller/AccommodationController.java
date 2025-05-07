package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.accommodation.service.AccommodationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 숙소 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
public class AccommodationController {
    private final AccommodationService accommodationService;

    /**
     * 숙소 목록 페이지로 이동합니다.
     */
    @GetMapping("/list")
    public String accommodationList(
            @RequestParam(value = "sidoCode", required = false) Integer sidoCode,
            @RequestParam(value = "gugunCode", required = false) Integer gugunCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        try {
            List<Accommodation> accommodations;

            if (keyword != null && !keyword.isEmpty()) {
                // 키워드 검색
                accommodations = accommodationService.searchAccommodations(keyword);
                model.addAttribute("keyword", keyword);
            } else if (sidoCode != null) {
                // 지역 기반 검색
                accommodations = accommodationService.getAccommodationsByRegion(sidoCode, gugunCode);
                model.addAttribute("sidoCode", sidoCode);
                model.addAttribute("gugunCode", gugunCode);
            } else {
                // 모든 숙소 조회
                accommodations = accommodationService.getAllAccommodations();
            }

            model.addAttribute("accommodations", accommodations);
            return "accommodation/list";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 숙소 상세 페이지로 이동합니다.
     */
    @GetMapping("/detail/{accommodationId}")
    public String accommodationDetail(@PathVariable Long accommodationId, Model model) {
        try {
            Accommodation accommodation = accommodationService.getAccommodationById(accommodationId);
            List<Room> rooms = accommodationService.getRoomsByAccommodationId(accommodationId);

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
     * 객실 상세 페이지로 이동합니다.
     */
    @GetMapping("/room/{roomId}")
    public String roomDetail(@PathVariable Long roomId, Model model) {
        try {
            Room room = accommodationService.getRoomById(roomId);
            Accommodation accommodation = accommodationService.getAccommodationById(room.getAccommodationId());

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
     * 호스트의 숙소 목록 페이지로 이동합니다.
     */
    @GetMapping("/host/accommodations")
    public String hostAccommodations(HttpSession session, Model model) {
        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            model.addAttribute("accommodations", accommodations);
            return "accommodation/host-accommodations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 숙소 등록 폼 페이지로 이동합니다.
     */
    @GetMapping("/register-form")
    public String registerForm(HttpSession session, Model model) {
        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        return "accommodation/register-form";
    }

    /**
     * 객실 등록 폼 페이지로 이동합니다.
     */
    @GetMapping("/register-room-form/{accommodationId}")
    public String registerRoomForm(@PathVariable Long accommodationId, HttpSession session, Model model) {
        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            Accommodation accommodation = accommodationService.getAccommodationById(accommodationId);

            // 해당 숙소의 호스트인지 확인
            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 객실을 등록할 수 있습니다.");
                return "error";
            }

            model.addAttribute("accommodation", accommodation);
            return "accommodation/register-room-form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 새 숙소를 등록합니다.
     * 숙소 정보와 이미지는 필수입니다.
     */
    @PostMapping("/register")
    public String registerAccommodation(
            @ModelAttribute Accommodation accommodation,
            @RequestParam("imageFiles") List<MultipartFile> imageFiles,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            // 필수 필드 검증
            if (accommodation.getTitle() == null || accommodation.getTitle().trim().isEmpty()) {
                model.addAttribute("error", "숙소 이름은 필수 항목입니다.");
                model.addAttribute("accommodation", accommodation);
                return "accommodation/register-form";
            }

            if (accommodation.getDescription() == null || accommodation.getDescription().trim().isEmpty()) {
                model.addAttribute("error", "숙소 설명은 필수 항목입니다.");
                model.addAttribute("accommodation", accommodation);
                return "accommodation/register-form";
            }

            if (accommodation.getAddress() == null || accommodation.getAddress().trim().isEmpty()) {
                model.addAttribute("error", "주소는 필수 항목입니다.");
                model.addAttribute("accommodation", accommodation);
                return "accommodation/register-form";
            }

            if (accommodation.getSidoCode() == null || accommodation.getGugunCode() == null) {
                model.addAttribute("error", "지역 정보(시도, 구군)는 필수 항목입니다.");
                model.addAttribute("accommodation", accommodation);
                return "accommodation/register-form";
            }

            // 호스트 ID 설정
            accommodation.setHostId(hostId);

            // 상태 설정 (기본값: PENDING_REVIEW)
            accommodation.setStatus("PENDING_REVIEW");

            // 이미지 처리 (실제 구현에서는 이미지 업로드 로직 추가 필요)
            List<Image> images = new ArrayList<>();
            boolean hasValidImage = false;

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (int i = 0; i < imageFiles.size(); i++) {
                    MultipartFile file = imageFiles.get(i);
                    if (!file.isEmpty()) {
                        hasValidImage = true;
                        // 이미지 업로드 및 URL 생성 로직 (실제 구현 필요)
                        String imageUrl = "images/accommodation/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

                        Image image = new Image();
                        image.setImageUrl(imageUrl);
                        image.setCaption(accommodation.getTitle() + " 이미지 " + (i + 1));
                        image.setIsMain(i == 0); // 첫 번째 이미지를 대표 이미지로 설정

                        images.add(image);
                    }
                }
            }

            // 이미지가 없는 경우 에러 반환
            if (!hasValidImage) {
                model.addAttribute("error", "최소 한 개 이상의 숙소 이미지가 필요합니다.");
                model.addAttribute("accommodation", accommodation);
                return "accommodation/register-form";
            }

            // 숙소 등록
            Long accommodationId = accommodationService.registerAccommodation(accommodation, images);

            redirectAttributes.addFlashAttribute("message", "숙소가 등록되었습니다. 관리자 승인 후 활성화됩니다.");
            return "redirect:/accommodation/host/accommodations";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("accommodation", accommodation);
            return "accommodation/register-form";
        }
    }

    /**
     * 새 객실을 등록합니다.
     * 객실 정보와 이미지는 필수입니다.
     */
    @PostMapping("/register-room")
    public String registerRoom(
            @ModelAttribute Room room,
            @RequestParam("imageFiles") List<MultipartFile> imageFiles,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            // 해당 숙소의 호스트인지 확인
            Accommodation accommodation = accommodationService.getAccommodationById(room.getAccommodationId());
            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 객실을 등록할 수 있습니다.");
                return "error";
            }

            // 필수 필드 검증
            if (room.getName() == null || room.getName().trim().isEmpty()) {
                model.addAttribute("error", "객실 이름은 필수 항목입니다.");
                model.addAttribute("room", room);
                return "accommodation/register-room-form";
            }

            if (room.getDescription() == null || room.getDescription().trim().isEmpty()) {
                model.addAttribute("error", "객실 설명은 필수 항목입니다.");
                model.addAttribute("room", room);
                return "accommodation/register-room-form";
            }

            if (room.getPrice() == null || room.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                model.addAttribute("error", "객실 가격은 필수 항목이며 0보다 커야 합니다.");
                model.addAttribute("room", room);
                return "accommodation/register-room-form";
            }

            if (room.getCapacity() == null || room.getCapacity() <= 0) {
                model.addAttribute("error", "수용 인원은 필수 항목이며 0보다 커야 합니다.");
                model.addAttribute("room", room);
                return "accommodation/register-room-form";
            }

            // 상태 설정 (기본값: AVAILABLE)
            room.setStatus("AVAILABLE");

            // 이미지 처리 (실제 구현에서는 이미지 업로드 로직 추가 필요)
            List<Image> images = new ArrayList<>();
            boolean hasValidImage = false;

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (int i = 0; i < imageFiles.size(); i++) {
                    MultipartFile file = imageFiles.get(i);
                    if (!file.isEmpty()) {
                        hasValidImage = true;
                        // 이미지 업로드 및 URL 생성 로직 (실제 구현 필요)
                        String imageUrl = "images/room/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

                        Image image = new Image();
                        image.setImageUrl(imageUrl);
                        image.setCaption(room.getName() + " 이미지 " + (i + 1));
                        image.setIsMain(i == 0); // 첫 번째 이미지를 대표 이미지로 설정

                        images.add(image);
                    }
                }
            }

            // 이미지가 없는 경우 에러 반환
            if (!hasValidImage) {
                model.addAttribute("error", "최소 한 개 이상의 객실 이미지가 필요합니다.");
                model.addAttribute("room", room);
                return "accommodation/register-room-form";
            }

            // 객실 등록
            Long roomId = accommodationService.registerRoom(room, images);

            redirectAttributes.addFlashAttribute("message", "객실이 등록되었습니다.");
            return "redirect:/accommodation/detail/" + room.getAccommodationId();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("room", room);
            return "accommodation/register-room-form";
        }
    }

    /**
     * 숙소 수정 폼 페이지로 이동합니다.
     */
    @GetMapping("/update-form/{accommodationId}")
    public String updateForm(@PathVariable Long accommodationId, HttpSession session, Model model) {
        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            Accommodation accommodation = accommodationService.getAccommodationById(accommodationId);

            // 해당 숙소의 호스트인지 확인
            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 수정할 수 있습니다.");
                return "error";
            }

            model.addAttribute("accommodation", accommodation);
            return "accommodation/update-form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 객실 수정 폼 페이지로 이동합니다.
     */
    @GetMapping("/update-room-form/{roomId}")
    public String updateRoomForm(@PathVariable Long roomId, HttpSession session, Model model) {
        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            Room room = accommodationService.getRoomById(roomId);
            Accommodation accommodation = accommodationService.getAccommodationById(room.getAccommodationId());

            // 해당 숙소의 호스트인지 확인
            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 객실을 수정할 수 있습니다.");
                return "error";
            }

            model.addAttribute("room", room);
            model.addAttribute("accommodation", accommodation);
            return "accommodation/update-room-form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 숙소 정보를 업데이트합니다.
     */
    @PostMapping("/update")
    public String updateAccommodation(
            @ModelAttribute Accommodation accommodation,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            // 해당 숙소의 호스트인지 확인
            Accommodation existingAccommodation = accommodationService.getAccommodationById(accommodation.getAccommodationId());
            if (!existingAccommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 수정할 수 있습니다.");
                return "error";
            }

            // 이미지 처리 (실제 구현에서는 이미지 업로드 로직 추가 필요)
            List<Image> images = null;
            if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
                images = new ArrayList<>();
                for (int i = 0; i < imageFiles.size(); i++) {
                    MultipartFile file = imageFiles.get(i);
                    if (!file.isEmpty()) {
                        // 이미지 업로드 및 URL 생성 로직 (실제 구현 필요)
                        String imageUrl = "images/accommodation/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

                        Image image = new Image();
                        image.setImageUrl(imageUrl);
                        image.setCaption(accommodation.getTitle() + " 이미지 " + (i + 1));
                        image.setIsMain(i == 0); // 첫 번째 이미지를 대표 이미지로 설정

                        images.add(image);
                    }
                }
            }

            // 숙소 업데이트
            accommodationService.updateAccommodation(accommodation, images);

            redirectAttributes.addFlashAttribute("message", "숙소 정보가 업데이트되었습니다.");
            return "redirect:/accommodation/detail/" + accommodation.getAccommodationId();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("accommodation", accommodation);
            return "accommodation/update-form";
        }
    }

    /**
     * 객실 정보를 업데이트합니다.
     */
    @PostMapping("/update-room")
    public String updateRoom(
            @ModelAttribute Room room,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            // 해당 숙소의 호스트인지 확인
            Room existingRoom = accommodationService.getRoomById(room.getRoomId());
            Accommodation accommodation = accommodationService.getAccommodationById(existingRoom.getAccommodationId());

            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 객실을 수정할 수 있습니다.");
                return "error";
            }

            // 이미지 처리 (실제 구현에서는 이미지 업로드 로직 추가 필요)
            List<Image> images = null;
            if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
                images = new ArrayList<>();
                for (int i = 0; i < imageFiles.size(); i++) {
                    MultipartFile file = imageFiles.get(i);
                    if (!file.isEmpty()) {
                        // 이미지 업로드 및 URL 생성 로직 (실제 구현 필요)
                        String imageUrl = "images/room/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

                        Image image = new Image();
                        image.setImageUrl(imageUrl);
                        image.setCaption(room.getName() + " 이미지 " + (i + 1));
                        image.setIsMain(i == 0); // 첫 번째 이미지를 대표 이미지로 설정

                        images.add(image);
                    }
                }
            }

            // 객실 업데이트
            accommodationService.updateRoom(room, images);

            redirectAttributes.addFlashAttribute("message", "객실 정보가 업데이트되었습니다.");
            return "redirect:/accommodation/room/" + room.getRoomId();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("room", room);
            return "accommodation/update-room-form";
        }
    }

    /**
     * 숙소를 삭제합니다.
     */
    @GetMapping("/delete/{accommodationId}")
    public String deleteAccommodation(
            @PathVariable Long accommodationId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            // 해당 숙소의 호스트인지 확인
            Accommodation accommodation = accommodationService.getAccommodationById(accommodationId);
            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 삭제할 수 있습니다.");
                return "error";
            }

            // 숙소 삭제
            accommodationService.deleteAccommodation(accommodationId);

            redirectAttributes.addFlashAttribute("message", "숙소가 삭제되었습니다.");
            return "redirect:/accommodation/host/accommodations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 객실을 삭제합니다.
     */
    @GetMapping("/delete-room/{roomId}")
    public String deleteRoom(
            @PathVariable Long roomId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || !"HOST".equals(role)) {
            return "redirect:/user/login-form";
        }

        try {
            // 해당 숙소의 호스트인지 확인
            Room room = accommodationService.getRoomById(roomId);
            Accommodation accommodation = accommodationService.getAccommodationById(room.getAccommodationId());

            if (!accommodation.getHostId().equals(hostId)) {
                model.addAttribute("error", "해당 숙소의 호스트만 객실을 삭제할 수 있습니다.");
                return "error";
            }

            // 객실 삭제
            accommodationService.deleteRoom(roomId);

            redirectAttributes.addFlashAttribute("message", "객실이 삭제되었습니다.");
            return "redirect:/accommodation/detail/" + accommodation.getAccommodationId();
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
    public String filterAccommodations(
            @RequestParam(value = "sidoCode", required = false) Integer sidoCode,
            @RequestParam(value = "gugunCode", required = false) Integer gugunCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Model model) {

        try {
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

            List<Accommodation> accommodations = accommodationService.getFilteredAccommodations(filters);

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
