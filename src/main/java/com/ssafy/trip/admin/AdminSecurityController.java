package com.ssafy.trip.admin;

import com.ssafy.trip.config.PasswordMigrationUtil;
import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;

/**
 * 관리자 보안 컨트롤러
 * 보안 관련 관리자 기능을 제공합니다.
 */
@Controller
@RequestMapping("/admin/security")
public class AdminSecurityController {

    private final PasswordMigrationUtil passwordMigrationUtil;
    private final UserService userService;

    public AdminSecurityController(PasswordMigrationUtil passwordMigrationUtil, UserService userService) {
        this.passwordMigrationUtil = passwordMigrationUtil;
        this.userService = userService;
    }

    /**
     * 보안 관리 페이지를 표시합니다.
     * 
     * @return 보안 관리 페이지 뷰 이름
     */
    @GetMapping
    public String securityManagement() {
        return "admin/security-management";
    }

    /**
     * 모든 사용자의 비밀번호를 마이그레이션합니다.
     * 
     * @param redirectAttributes 리다이렉트 속성
     * @return 리다이렉트 URL
     */
    @PostMapping("/migrate-passwords")
    public String migratePasswords(RedirectAttributes redirectAttributes) {
        int count = passwordMigrationUtil.migrateAllPasswords();
        redirectAttributes.addFlashAttribute("message", count + "명의 사용자 비밀번호가 마이그레이션되었습니다.");
        return "redirect:/admin/security";
    }

    /**
     * 비밀번호 마이그레이션 상태를 확인합니다.
     * 
     * @param model 모델
     * @return 비밀번호 마이그레이션 상태 페이지 뷰 이름
     */
    @GetMapping("/password-migration-status")
    public String passwordMigrationStatus(Model model) {
        try {
            // 전체 사용자 목록 조회
            List<User> users = userService.getUserList();
            int totalUsers = users.size();

            // 마이그레이션된 사용자 수 계산
            int migratedUsers = 0;
            for (User user : users) {
                if (!passwordMigrationUtil.needsMigration(user.getPassword())) {
                    migratedUsers++;
                }
            }

            // 모델에 데이터 추가
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("migratedUsers", migratedUsers);

            return "admin/password-migration-status";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "사용자 정보를 조회하는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }
}
