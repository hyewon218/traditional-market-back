package com.market.global.controller;

import com.market.domain.member.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberServiceImpl memberService;

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/members/login")
    public String loginPage() {
        return "members/login";
    }

    @GetMapping("/members/testLogin")
    public String testLoginPage() {
        return "members/testLogin";
    }

    // 로그인 성공
    @GetMapping("/success")
    public String success() {
        return "members/success";
    }

    // OAuth2 로그인 성공 시 추가정보 입력페이지
    @GetMapping("/auth/success")
    public String authSuccess2() {
        return "auth/addInfo";
    }

    @GetMapping("/kakaomap")
    public String kakaoMap() {
        return "kakaomap/map";
    }

    @GetMapping("/navermap")
    public String naverMap() {
        return "navermap/map";
    }

}
