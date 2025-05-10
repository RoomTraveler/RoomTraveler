package com.ssafy.trip.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 에러 처리 컨트롤러
 * 에러 페이지 요청을 처리합니다.
 */
@Controller
public class ErrorController {

    /**
     * 접근 거부 페이지를 표시합니다.
     * 
     * @return 접근 거부 페이지 뷰 이름
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}