package com.market.global.controller;

import com.market.domain.member.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberServiceImpl memberService;

    @GetMapping("/")
    public String mainPage() {
        return "mainTest";
    }

    @GetMapping("/members/signup")
    public String signup() {
        return "members/signup";
    }

    @GetMapping("/members/login")
    public String loginPage() {
        return "members/login";
    }

    // OAuth2 로그인 성공 시 추가정보 입력페이지
    @GetMapping("/oauth/success")
    public String authSuccess2() {
        return "oauth/addInfo";
    }

    @GetMapping("/kakaomap")
    public String kakaoMap() {
        return "kakaomap/map";
    }

    @GetMapping("/map")
    public String naverMapAndWeather() {
        return "navermapandweather/mapWeather";
    }

    // 공지사항
    @GetMapping("/notice")
    public String noticeMain() {
        return "notice/main";
    }

    // 문의사항
    @GetMapping("/inquiry")
    public String inquiryMain() {
        return "inquiry/main";
    }

    // 관리자 페이지
    @GetMapping("/admin")
    public String adminMain() {
        return "admin/main";
    }

    // 장바구니
    @GetMapping("/cart")
    public String cartMain() {
        return "cart/main";
    }

    // 내정보 페이지
    @GetMapping("/myinfo")
    public String myinfoMain() {
        return "members/myinfo";
    }

    // 시장 찾기 메뉴
    @GetMapping("/market")
    public String marketMain() {
        return "market/main";
    }

    // 시장 추가(관리자 권한)
    @GetMapping("/market/add")
    public String marketAdd() {
        return "market/addMarket";
    }

    // 401 에러
    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }

    // 403 에러
    @GetMapping("/forbidden")
    public String forbidden() {
        return "forbidden";
    }

    // 특정 시장 조회
    @GetMapping("/markets/{marketNo}")
    public String marketDetail(@PathVariable Long marketNo) {
        return "market/marketInfo";
    }


}
