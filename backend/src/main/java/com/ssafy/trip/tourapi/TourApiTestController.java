package com.ssafy.trip.tourapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 한국관광공사 API 테스트 페이지를 제공하는 컨트롤러
 */
@Controller
@RequestMapping("/tourapi")
public class TourApiTestController {

    /**
     * API 테스트 페이지 제공
     * @return 테스트 페이지 뷰 이름
     */
    @GetMapping("/test")
    public String showTestPage() {
        return "tourapi/test";
    }
}