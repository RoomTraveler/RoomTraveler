package com.ssafy.trip.accommodation;

import com.ssafy.trip.tourapi.TourApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AccommodationController {

    private final TourApiProperties props;

    @GetMapping("/accommodation")
    public String showPage(Model model) {

        return "accommodation/index";
    }
}
