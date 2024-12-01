package com.market.domain.member.controller;

import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.*;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberSearchCond;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import com.market.global.security.oauth2.ProviderType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberServiceImpl memberService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> createMember(
        @Valid @RequestBody MemberRequestDto memberRequestDto, HttpServletRequest request) {
        memberService.createMember(memberRequestDto, request);
        return ResponseEntity.ok().body(new ApiResponse("회원가입을 축하드립니다!", HttpStatus.OK.value()));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> logIn(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, @RequestBody MemberRequestDto requestDto)
        throws Exception {
        MemberResponseDto responseDto = memberService.logIn(httpServletRequest,
            httpServletResponse, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {
        memberService.logOut(httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("로그아웃 성공", HttpStatus.OK.value()));
    }

    // 전체 회원 조회(admin만 가능)
    @GetMapping("")
    public ResponseEntity<Page<MemberResponseDto>> findAllMember(Pageable pageable) {
        Page<MemberResponseDto> members = memberService.findAll(pageable);
        return ResponseEntity.ok().body(members);
    }

    // 키워드 검색 회원 목록 조회
    @GetMapping("/search")
    public ResponseEntity<Page<MemberResponseDto>> searchMembers(MemberSearchCond cond,
        Pageable pageable) {
        Page<MemberResponseDto> searchMembers = memberService.searchMembers(cond, pageable);
        return ResponseEntity.ok().body(searchMembers);
    }

    // 특정 회원 조회(일반 회원이 자신의 상세정보 열람)
    @GetMapping("/myinfo")
    public ResponseEntity<MyInfoResponseDto> myInfo(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = memberService.findById(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok().body(MyInfoResponseDto.of(member));
    }

    // memberNo 이용한 특정 회원 조회(관리자만 가능)
    @GetMapping("/myinfo/{memberNo}")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long memberNo) {
        memberService.validationAdmin(userDetails.getMember());
        return ResponseEntity.ok().body(MemberResponseDto.of(memberService.findById(memberNo)));
    }

    // memberId 이용한 특정 회원 조회(관리자만 가능)
    @GetMapping("/info-id")
    public ResponseEntity<MemberResponseDto> getMemberById(String memberId) {
        return ResponseEntity.ok()
            .body(MemberResponseDto.of(memberService.findByMemberId(memberId)));
    }

    // 회원 수정
    @PutMapping("")
    public ResponseEntity<Member> updateMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody MemberRequestDto memberRequestDto) {
        Member updatedMember = memberService.update(userDetails.getMember().getMemberNo(),
            memberRequestDto);
        return ResponseEntity.ok().body(updatedMember);
    }

    // 닉네임 변경까지 남은 시간
    @GetMapping("/change-nickname-time/{memberNo}")
    public ResponseEntity<String> allowedChangeTime(@PathVariable Long memberNo) {
        return ResponseEntity.ok().body(memberService.getRemainingTime(memberNo));
    }

    // 회원 권한 변경
    @PutMapping("/admin/u/{memberNo}")
    public ResponseEntity<?> updateMemberRole(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody MemberRequestDto memberRequestDto, @PathVariable Long memberNo) {
        memberService.updateRole(userDetails.getMember(), memberNo, memberRequestDto);
        return ResponseEntity.ok().body(new ApiResponse("회원 권한 변경 성공", HttpStatus.OK.value()));
    }

    // 회원 제재
    @PutMapping("/admin/warning/{memberNo}")
    public ResponseEntity<ApiResponse> warningMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long memberNo) {
        memberService.warningMember(userDetails.getMember(), memberNo);
        return ResponseEntity.ok().body(new ApiResponse("회원 제재 성공", HttpStatus.OK.value()));
    }

    // 회원 제재 해제
    @PutMapping("/admin/warningclear/{memberNo}")
    public ResponseEntity<ApiResponse> warningClear(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long memberNo) {
        memberService.warningClear(userDetails.getMember(), memberNo);
        return ResponseEntity.ok().body(new ApiResponse("회원 제재 해제 성공", HttpStatus.OK.value()));
    }

    // 회원 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse> deleteMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {
        memberService.deleteMember(userDetails.getMember().getMemberNo(),
            userDetails.getMember().getMemberId(), httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // 회원 삭제(admin이 다른 회원 삭제)
    @DeleteMapping("/admin/r/{memberNo}")
    public ResponseEntity<?> deleteMemberAdmin(@AuthenticationPrincipal UserDetailsImpl userDetails,
        HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        @PathVariable Long memberNo) {

        Member member = memberService.findById(memberNo);
        memberService.deleteMemberAdmin(userDetails.getMember(),
            member.getMemberNo(), member.getMemberId(), httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // OAuth2 인증 성공 후 추가 정보 수정 실행(memberNickname)
    @PutMapping("/addinfo")
    public ResponseEntity<Member> addInfo(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody MemberRequestDto memberRequestDto) {
        Member updateOauth2Member = memberService.updateOAuthAddInfo(
            userDetails.getMember().getMemberNo(), memberRequestDto);
        return ResponseEntity.ok().body(updateOauth2Member);
    }

    // 회원가입 시 인증번호 일치하는지 확인(검증)
    @PostMapping("/verifycode")
    public ResponseEntity<String> verifyCode(
        @RequestBody VerifyCodeRequestDto verifyCodeRequestDto) {
        memberService.verifyCode(verifyCodeRequestDto.getMemberEmail(),
            verifyCodeRequestDto.getCode());
        return ResponseEntity.ok().body(verifyCodeRequestDto.getCode());
    }

    // 아이디 찾기
    @PostMapping("/findid")
    public ResponseEntity<String> findMemberId(@RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseEntity.ok().body(memberService.findIdByEmail(findIdRequestDto));
    }

    // 배포 전 로그 지우기
    // 비밀번호 변경
    @PutMapping("/changepw")
    public ResponseEntity<String> changeMemberPw(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody ChangePwRequestDto changePwRequestDto) {
        memberService.changePassword(userDetails.getMember().getMemberNo(),
            changePwRequestDto.getChangePw(), changePwRequestDto.getConfirmPw());
        return ResponseEntity.ok()
            .body("비밀번호 변경 성공, 변경한 비밀번호 : " + changePwRequestDto.getChangePw());
    }

    // 회원가입 시 아이디 중복 확인 및 탈퇴 회원에서 아이디 검증
    @GetMapping("/checkid")
    public ResponseEntity<ApiResponse> existsMemberId(String memberId) {
        memberService.validationId(memberId);
        return ResponseEntity.ok()
            .body(new ApiResponse("사용 가능한 아이디입니다.", HttpStatus.OK.value()));
    }

    // 회원가입 시 이메일 중복 확인 및 탈퇴 회원에서 이메일 검증
    @GetMapping("/checkemail")
    public ResponseEntity<ApiResponse> existsMemberEmail(String memberEmail) {
        memberService.validationEmail(memberEmail);
        return ResponseEntity.ok()
            .body(new ApiResponse("사용 가능한 이메일입니다.", HttpStatus.OK.value()));
    }

    // 내정보 열람 시 본인 비밀번호 확인
    @PostMapping("/myinfo/check")
    public ResponseEntity<ApiResponse> verifyPassword(
        @AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request,
        HttpServletResponse response, String password) {
        memberService.checkPassword(request, response, password,
            userDetails.getMember().getMemberNo());
        return ResponseEntity.ok().body(new ApiResponse("성공", HttpStatus.OK.value()));
    }

    // 권한 조회(admin만 가능)
    @GetMapping("/admin/role")
    public ResponseEntity<Page<MyInfoResponseDto>> getRole(Role role, Pageable pageable) {
        return ResponseEntity.ok().body(memberService.getRole(role, pageable));
    }

    // 총 회원 수
    @GetMapping("/admin/count")
    public ResponseEntity<?> countMembers() {
        return ResponseEntity.ok().body(memberService.countMembers());
    }

    // providerType별 회원 수
    @GetMapping("/count-providertype")
    public ResponseEntity<Map<ProviderType, Long>> getCountByProviderType() {
        return ResponseEntity.ok().body(memberService.getCountByProviderType());
    }

    // 다른 회원 신고 (댓글에서 사용)
    @PostMapping("/report")
    public ResponseEntity<ApiResponse> postReport(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody MemberRequestDto requestDto) {
        memberService.reportMember(userDetails.getMember(), requestDto);
        return ResponseEntity.ok().body(new ApiResponse("신고 완료", HttpStatus.OK.value()));
    }

    // 내가 신고한 사람 목록 확인, 관리자만 가능
    @GetMapping("/report-list")
    public ResponseEntity<String> getReportList(Long memberNo) {
        return ResponseEntity.ok().body(memberService.getReportMemberList(memberNo));
    }

    // 나를 신고한 사람 목록 확인, 관리자만 가능
    @GetMapping("/report-list/who")
    public ResponseEntity<String> getReporters(Long memberNo) {
        return ResponseEntity.ok().body(memberService.getReporterList(memberNo));
    }

    @PostMapping("/make-cookie")
    public ResponseEntity<ApiResponse> postCookie(HttpServletRequest request,
        HttpServletResponse response, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        memberService.setPasswordVerifiedToCookie(request, response, member.getRandomTag());
        return ResponseEntity.ok().body(
            new ApiResponse("isPasswordVerified 쿠키 생성", HttpStatus.OK.value()));
    }

    @GetMapping("/has-cookie")
    public ResponseEntity<Boolean> getCookie(HttpServletRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Boolean hasCookie = memberService.isPasswordVerified(request, member.getRandomTag());
        return ResponseEntity.ok().body(hasCookie);
    }
}
