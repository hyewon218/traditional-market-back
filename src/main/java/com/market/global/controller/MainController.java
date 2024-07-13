package com.market.global.controller;

import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.service.InquiryServiceImpl;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberServiceImpl memberService;
    private final InquiryServiceImpl inquiryService;

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

    // 내정보 페이지 홈 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo")
    public String myinfoMain() {
        return "members/myinfo";
    }

    // 내정보 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/myinfo")
    public String getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        String randomTag = userDetails.getMember().getRandomTag();
        if (memberService.isPasswordVerified(request, randomTag)) {
            return "members/myinfo/myinfo";
        } else {
            return "members/myinfo/checkpassword";
        }
    }

    // 구매목록 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/list")
    public String getMypurchaseList() {
        return "members/myinfo/list";
    }

    // 배송지 관리 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/delivery")
    public String getMyDeliveryList() {
        return "members/myinfo/mydelivery";
    }

    // 내 문의사항 목록 보기 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/inquiries")
    public String getMyInquiryList() {
        return "members/myinfo/inquiries";
    }

    // 내 문의사항 개별 보기 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/inquiry/{inquiryNo}")
    public String getMyInquiry(@PathVariable Long inquiryNo,
                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Inquiry inquiry = inquiryService.findById(inquiryNo);

        if (member.getMemberNo() == inquiry.getMemberNo()) {
            return "inquiry/inquiryinfo";
        } else {
            return "unauthorized";
        }
    }

    // 배송지 관리
    @GetMapping("/myinfo/deliveries")
    public String getDeliveries() {
        return "members/myinfo/mydelivery";
    }

    // 회원 탈퇴
    @GetMapping("/myinfo/v")
    public String getVerification() {
        return "members/myinfo/delete";
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
