package com.ssafy.trip.tourapi;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tourapi")
public class StayController {

    private final TourApiProperties props;

    public StayController(TourApiProperties props) {
        this.props = props;
    }

    @GetMapping("/index")
    public String showStayPage(Model model) {
        // JSP에서 ${serviceKey} 로 꺼낼 수 있도록 담아둡니다.
        model.addAttribute("serviceKey", props.getServiceKey());
        return "accommodation/index";
    }
}
