package com.market.domain.member.controller;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import com.market.global.security.handler.ApiLoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberServiceImpl memberService;
    private final ApiLoginSuccessHandler apiLoginSuccessHandler;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Member> createMember(@RequestBody MemberRequestDto memberRequestDto) {

        Member savedMember = memberService.createMember(memberRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMember);
    }

    // 로그인, ApiLoginSuccessHandler 클래스 통한 로그인 성공 후 처리
    @PostMapping("/login")
    public void logIn(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                      @RequestBody MemberRequestDto requestDto) throws Exception {
        Authentication authentication = memberService.logIn(httpServletRequest, httpServletResponse, requestDto);

        apiLoginSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(new ApiResponse("로그아웃 성공", HttpStatus.OK.value()));
    }

    // 전체 회원 조회
    @GetMapping("")
    public ResponseEntity<List<MemberResponseDto>> findAllMember() {

        List<MemberResponseDto> members = memberService.findAll()
                .stream()
                .map(MemberResponseDto::new)
                .toList();

        return ResponseEntity.ok()
                .body(members);
    }
    
    // 특정 회원 조회
    @GetMapping("/1")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = memberService.findById(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok()
                .body(new MemberResponseDto(member));
    }

    // 회원 수정
    @PutMapping("")
    public ResponseEntity<Member> updateMember(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody MemberRequestDto memberRequestDto) {
        Member updatedMember = memberService.update(userDetails.getMember().getMemberNo(), memberRequestDto);
        return ResponseEntity.ok()
                .body(updatedMember);
    }

    // 회원 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse> deleteMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        memberService.deleteMember(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }


}
