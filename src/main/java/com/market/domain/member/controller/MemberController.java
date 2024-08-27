package com.market.domain.member.controller;

import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.*;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.repository.MemberSearchCond;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.exception.BusinessException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberServiceImpl memberService;
    private final MemberRepository memberRepository;

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
        HttpServletResponse httpServletResponse, @RequestBody MemberRequestDto requestDto) {
        try {
            MemberResponseDto responseDto = memberService.logIn(httpServletRequest,
                httpServletResponse, requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 틀렸습니다.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        Page<MemberResponseDto> result = memberService.searchMembers(cond, pageable);
        return ResponseEntity.ok().body(result);
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
        Member member;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        // admin 권한일 경우
        if (isAdmin) {
            member = memberService.findById(memberNo);
            return ResponseEntity.ok().body(MemberResponseDto.of(member));

        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
    }

    // memberId 이용한 특정 회원 조회(관리자만 가능)
    @GetMapping("/info-id")
    public ResponseEntity<MemberResponseDto> getMemberById(String memberId) {
        MemberResponseDto memberResponseDto = memberService.getMemberById(memberId);
        return ResponseEntity.ok().body(memberResponseDto);
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

    // 회원 권한 수정
    @PutMapping("/admin/u/{memberNo}")
    public ResponseEntity<?> updateMemberRole(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody MemberRequestDto memberRequestDto, @PathVariable Long memberNo) {
        Member member;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        // admin 권한일 경우
        if (isAdmin) {
            member = memberService.findById(memberNo);
            memberService.updateRole(member.getMemberNo(), memberRequestDto);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
        return ResponseEntity.ok().body(MemberResponseDto.of(member));
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
        Member member;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        // admin 권한일 경우
        if (isAdmin) {
            member = memberService.findById(memberNo);
            memberService.deleteMemberAdmin(member.getMemberNo(), member.getMemberId(), httpRequest,
                httpResponse);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
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
        if (memberService.verifyCode(verifyCodeRequestDto.getMemberEmail(),
            verifyCodeRequestDto.getCode())) {
            return ResponseEntity.ok(verifyCodeRequestDto.getCode());
        } else {
            return ResponseEntity.badRequest().body("인증번호가 일치하지않습니다.");
        }
    }

    // 아이디 찾기
    @PostMapping("/findid")
    public ResponseEntity<String> findMemberId(@RequestBody FindIdRequestDto findIdRequestDto) {
        String foundMemberId = memberService.findIdByEmail(findIdRequestDto);
        if (foundMemberId != null) {
            return ResponseEntity.ok().body(foundMemberId);
        } else {
            return ResponseEntity.badRequest().body("아이디 찾기 실패");
        }
    }

    // 비밀번호 변경
    @PutMapping("/changepw")
    public ResponseEntity<String> changeMemberPw(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody ChangePwRequestDto changePwRequestDto) {
        Optional<Member> optionalMember = memberRepository.findById(
            userDetails.getMember().getMemberNo());
        if (optionalMember.isPresent()) {
            if (memberService.changePassword(userDetails.getMember().getMemberNo(),
                changePwRequestDto.getChangePw(), changePwRequestDto.getConfirmPw())) {
                return ResponseEntity.ok()
                    .body("비밀번호 변경 성공, 변경한 비밀번호 : " + changePwRequestDto.getChangePw());
            }
        }
        return ResponseEntity.badRequest().body("비밀번호 변경 실패");
    }

//    // 회원가입 시 아이디 중복 확인 및 탈퇴 회원에서 아이디 검증
//    @GetMapping("/checkid")
//    public ResponseEntity<ApiResponse> existsMemberId(String memberId) {
//        try {
//            memberService.validationId(memberId);
//            return ResponseEntity.ok()
//                .body(new ApiResponse("사용 가능한 아이디입니다.", HttpStatus.OK.value()));
//        } catch (BusinessException e) {
//            // 예외에 따라 다른 메시지를 반환
//            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
//                .body(new ApiResponse(e.getErrorCode().getMessage(),
//                    e.getErrorCode().getHttpStatus().value()));
//        }
//    }

    // 회원가입 시 아이디 중복 확인 및 탈퇴 회원에서 아이디 검증
    @GetMapping("/checkid")
    public ResponseEntity<ApiResponse> existsMemberId(String memberId) {
        memberService.validationId(memberId);
        return ResponseEntity.ok()
            .body(new ApiResponse("사용 가능한 아이디입니다.", HttpStatus.OK.value()));
    }

    // 회원가입 시 이메일 중복 확인 및 탈퇴 회원에서 이메일 검증
//    @GetMapping("/checkemail")
//    public ResponseEntity<ApiResponse> existsMemberEmail(String memberEmail) {
//        try {
//            memberService.validationEmail(memberEmail);
//            return ResponseEntity.ok()
//                .body(new ApiResponse("사용 가능한 이메일입니다.", HttpStatus.OK.value()));
//        } catch (BusinessException e) {
//            // 예외에 따라 다른 메시지를 반환
//            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
//                .body(new ApiResponse(e.getErrorCode().getMessage(),
//                    e.getErrorCode().getHttpStatus().value()));
//        }
//    }

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
        boolean isValid = memberService.checkPassword(request, response, password,
            userDetails.getMember().getMemberNo());
        if (isValid) {
            return ResponseEntity.ok().body(new ApiResponse("성공", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("비밀번호가 틀립니다", HttpStatus.BAD_REQUEST.value()));
        }
    }

    // 권한 조회(admin만 가능)
    @GetMapping("/admin/role")
    public ResponseEntity<Page<MyInfoResponseDto>> getRole(Role role, Pageable pageable) {
        Page<MyInfoResponseDto> members = memberService.getRole(role, pageable);
        return ResponseEntity.ok().body(members);
    }

    // 총 회원 수
    @GetMapping("/admin/count")
    public ResponseEntity<?> countMembers() {
        return ResponseEntity.ok().body(memberService.countMembers());
    }

    // 권한 admin인지 확인
    @GetMapping("/check-admin")
    public ResponseEntity<Map<String, Boolean>> checkAdminRole() {
        // 서비스에서 관리자 권한 확인
        boolean isAdmin = memberService.isAdmin();

        // 결과를 JSON 형식으로 반환
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAdmin", isAdmin);

        return ResponseEntity.ok(response);
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

    // 내가 신고한 사람 목록 확인, 관리자만 가능
    @GetMapping("/report-list/who")
    public ResponseEntity<String> getReporters(Long memberNo) {
        return ResponseEntity.ok().body(memberService.getReporterList(memberNo));
    }

}
