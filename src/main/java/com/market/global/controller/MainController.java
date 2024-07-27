package com.market.global.controller;

import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.service.InquiryServiceImpl;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // 내 문의사항 목록 보기 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/inquiries")
    public String getMyInquiryList() {
        return "members/myinfo/inquiries";
    }

    // 내 문의사항 개별 보기 // 인증 필요, 테스트 끝나면 security에서 바꾸기
//    @GetMapping("/myinfo/inquiry/{inquiryNo}")
//    public String getMyInquiry(@PathVariable Long inquiryNo,
//                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
//                authority -> authority.getAuthority().equals("ROLE_ADMIN"));
//        Member member = userDetails.getMember();
//        Inquiry inquiry = inquiryService.findById(inquiryNo);
//
//        if (isAdmin || member.getMemberNo() == inquiry.getMemberNo()) {
//            return "inquiry/inquiryinfo";
//        } else {
//            return "unauthorized";
//        }
//    }
    @GetMapping("/myinfo/inquiry/{inquiryNo}")
    public String getMyInquiry(@PathVariable Long inquiryNo,
                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        Member member = userDetails.getMember();
        Inquiry inquiry = inquiryService.findById(inquiryNo);

        if (isAdmin) {
            return "admin/inquiryinfo";
        } else if (member.getMemberNo().equals(inquiry.getMemberNo())) {
            return "inquiry/inquiryinfo";
        } else {
            return "unauthorized";
        }
    }

    // 공지사항 추가 페이지(관리자)
    @GetMapping("/admin/notice/a")
    public String addNotice() {
        return "notice/addNotice";
    }

    // 공지사항 관리 페이지(관리자)
    @GetMapping("/admin/notices")
    public String getNoticeManage() {
        return "admin/noticeManage";
    }

    // 공지사항 전체 조회(관리자 외)
    @GetMapping("/noticelist")
    public String getNotices() {
        return "notice/noticelist";
    }

    // 공지사항 개별 보기
    @GetMapping("/notice/{noticeNo}")
    public String getNotice() {
        return "notice/noticeinfo";
    }

    // 공지사항 수정 페이지
    @GetMapping("/admin/notices/{noticeNo}")
    public String getUpdateNoticePage() {
        return "notice/updateNotice";
    }

    // 배송지 관리 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/deliveries")
    public String getDeliveries() {
        return "members/myinfo/mydelivery";
    }

    // 회원 탈퇴 // 인증 필요, 테스트 끝나면 security에서 바꾸기
    @GetMapping("/myinfo/v")
    public String getVerification() {
        return "members/myinfo/delete";
    }

    // 전체 회원 목록 조회(admin만)
    @GetMapping("/admin/members")
    public String getMemberList() {
        return "admin/memberlist";
    }

    // 전체 문의사항 목록 조회(admin만)
    @GetMapping("/admin/inquiries")
    public String getInquiryList() {
        return "admin/inquirylist";
    }

    // 아이디 찾기
    @GetMapping("/members/findid")
    public String getFindId() {
        return "members/findid";
    }

    // 임시비밀번호 발급
    @GetMapping("/members/temppw")
    public String getTempPw() {
        return "members/temppassword";
    }

    // 배송지 목록
    @GetMapping("/delivery/deliverylist")
    public String getDeliveryLIst() {
        return "delivery/deliveryList";
    }

    // 배송지 추가
    @GetMapping("/delivery/add")
    public String addDelivery() {
        return "delivery/addDelivery";
    }

    // 배송지 수정
    @GetMapping("/delivery/deliverylist/{deliveryNo}")
    public String updateDelivery() {
        return "delivery/updateDelivery";
    }

    // 주문 페이지 테스트
    @GetMapping("/order/test")
    public String orderTest() {
        return "order/test";
    }

    // 문의사항 작성 페이지
    @GetMapping("/inquiry/add")
    public String getAddInquiry() {
        return "inquiry/addInquiry";
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
    public String marketDetail() {
        return "market/marketInfo";
    }

    @GetMapping("/kakaopay")
    public String payRequest() {
        return "kakaopay/kakaopay";
    }

    // 주문 후 주문 정보 페이지
    @GetMapping("/orderinfo")
    public String getOrderInfo() {
        return "order/orderinfo";
    }

    // 시장 관리 페이지
    @GetMapping("/admin/markets")
    public String getMarketManage() {
        return "admin/marketManage";
    }

    // 시장 추가 페이지
    @GetMapping("/admin/markets/a")
    public String addMarket() {
        return "admin/addMarket";
    }

    // 특정 시장 조회(관리자)
    @GetMapping("/admin/markets/{marketNo}")
    public String getMarket() {
        return "admin/marketinfo";
    }

    // 시장 수정 페이지
    @GetMapping("/admin/markets/u/{marketNo}")
    public String updateMarket() {
        return "admin/updateMarket";
    }

    // 제미나이 css 테스트 페이지
    @GetMapping("/admin/test")
    public String geminiTest() {
        return "admin/gmarket";
    }

    // 상점 관리 페이지
    @GetMapping("/admin/shops")
    public String getShopManage() {
        return "admin/shopManage";
    }

    // 상점 추가 페이지
    @GetMapping("/admin/shops/a")
    public String addShop() {
        return "admin/addShop";
    }

    // 특정 상점 조회
    @GetMapping("/shops/{shopNo}")
    public String shopDetail() {
        return "shop/shopInfo";
    }

    // 특정 상점 조회(관리자)
    @GetMapping("/admin/shops/{shopNo}")
    public String getShop() {
        return "admin/shopinfo";
    }

    // 상점 수정 페이지
    @GetMapping("/admin/shops/u/{shopNo}")
    public String updateShop() {
        return "admin/updateShop";
    }

    // 방문자 수 집계(관리자)
    @GetMapping("/admin/visitor")
    public String viewVisitorCount() {
        return "admin/visitorCount";
    }

}
