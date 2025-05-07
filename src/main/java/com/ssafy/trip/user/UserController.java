package com.ssafy.trip.user;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService uService;

    /**
     * 인덱스 페이지로 이동합니다.
     */
    @GetMapping({"", "/index"})
    public String index() {
    	return "index";
    }

    /**
     * 로그인 폼 페이지로 이동합니다.
     */
    @GetMapping("/login-form")
    public String loginForm() {
    	return "user/login-form";
    }

    /**
     * 사용자 등록 폼 페이지로 이동합니다.
     */
    @GetMapping("/regist-user-form")
    public String registUserForm() {
    	return "user/user-regist-form";
    }

    /**
     * 사용자 상세 페이지로 이동합니다.
     */
    @GetMapping("/user-detail")
    public String userDetail() {
    	return "user/user-detail";
    }

    /**
     * 사용자 수정 폼 페이지로 이동합니다.
     */
    @GetMapping("/user-update-form")
    public String userUpdateForm() {
    	return "user/user-update-form";
    }

    /**
     * 새 사용자를 등록합니다.
     */
    @PostMapping("/regist-user")
    public String registUser(@ModelAttribute User user, RedirectAttributes redir, Model model) {
    	// 비밀번호는 평문으로 저장합니다.
    	try {
			uService.registUser(user);
			redir.addFlashAttribute("alertMsg", "등록되었습니다. 로그인 후 사용해주세요.");
			return "redirect:/user/login-form";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", e.getMessage());
			return "/user/user-regist-form";
		}
    }

    /**
     * 로그인 처리를 합니다.
     */
    @PostMapping("/login")
    public String login(@ModelAttribute User user, HttpSession session, RedirectAttributes redir, Model model) {

    	try {
			// 이메일과 비밀번호로 사용자 조회
			User loginUser = uService.login(user.getEmail(), user.getPassword());

			// 사용자가 존재하지 않거나 사용자명이 없는 경우
			if (loginUser == null || loginUser.getUsername() == null) {
				throw new Exception("잘못된 입력!");
			}

			// 로그인 성공 시 세션에 사용자 정보 저장 (비밀번호 제외)
			session.setAttribute("userId", loginUser.getUserId());
			session.setAttribute("username", loginUser.getUsername());
			session.setAttribute("email", loginUser.getEmail());
			redir.addFlashAttribute("alertMsg", "로그인에 성공했습니다!");
			return "redirect:/";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", e.getMessage());
			return "/user/login-form";
		}
    }

    /**
     * 사용자 목록을 조회합니다.
     */
    @PostMapping("/list")
    public String userList(Model model) {
    	List<User> users;
		try {
			users = uService.getUserList();
			model.addAttribute("users", users);
			return "user/user-list";
		} catch (SQLException e) {
			e.printStackTrace();
			model.addAttribute("error", e.getMessage());
			return "/";
		}
    }

    /**
     * 사용자 정보를 업데이트합니다.
     */
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user, HttpSession session, Model model) {
    	// 비밀번호는 평문으로 저장합니다.
    	try {
    		uService.updateUser(user.getEmail(), user.getUsername(), user.getPassword());
    		session.setAttribute("username", user.getUsername());
    		return "user/user-detail";
    	} catch (Exception e) {
    		model.addAttribute("error", e.getMessage());
    		return "user/user-update-form";    		
    	}

    }

    /**
     * 사용자를 삭제합니다.
     */
    @GetMapping("/delete")
    public String deleteUser(HttpSession session, RedirectAttributes redir, Model model) {
    	String email = (String) session.getAttribute("email");
    	try {
			uService.deleteUser(email);
			session.invalidate();
			redir.addFlashAttribute("alertMsg", "회원탈퇴에 성공했습니다!");
			return "redirect:/";
		} catch (SQLException e) {
			e.printStackTrace();
			model.addAttribute("error", e.getMessage());
			return "/";
		}

    }

    /**
     * 로그아웃 처리를 합니다.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redir) {
    	session.invalidate();
    	redir.addFlashAttribute("alertMsg", "로그아웃 됐습니다!");
    	return "redirect:/";
    }

    /**
     * 비밀번호를 찾습니다.
     */
    @PostMapping("/find-password")
    public String findPassword(@ModelAttribute User user, Model model) {
    	try {
			String password = uService.findPassword(user.getUsername(), user.getEmail());
			model.addAttribute("password", password);
			return "user/login-form";
		} catch (SQLException e) {
			e.printStackTrace();
			model.addAttribute("error", e.getMessage());
			return "user/find-passord";
		}	
    }
}
