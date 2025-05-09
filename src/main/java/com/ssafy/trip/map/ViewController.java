// src/main/java/com/ssafy/trip/controller/ViewController.java
package com.ssafy.trip.map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/map")
public class ViewController {

    @GetMapping("/region-trip-form")
    public String regionTripForm() {
        return "trip/test";
    }

    @GetMapping("/plansPage")
    public String plansPage() {
        return "trip/plans";
    }
}
