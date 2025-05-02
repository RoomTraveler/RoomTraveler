package com.ssafy.trip.host;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;

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
            // 이 부분은 실제 구현 시 UserService에 updateRole 메서드를 추가해야 함
            
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
}