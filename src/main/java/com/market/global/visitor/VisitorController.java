package com.market.global.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    @GetMapping("/today")
    public long getTodayVisitorCount() {
        return visitorService.getTodayVisitorCount();
    }

    @GetMapping("/total")
    public long getTotalVisitorCount() {
        return visitorService.getTotalVisitorCount();
    }
}
