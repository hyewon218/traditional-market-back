package com.market.domain.member.controller;

import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.*;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.repository.MemberSearchCond;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Member> createMember(@RequestBody MemberRequestDto memberRequestDto) {
        Member savedMember = memberService.createMember(memberRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
    }
    
    // 로그인
    @PostMapping("/login")
    public void logIn(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                      @RequestBody MemberRequestDto requestDto) throws Exception {
        Authentication authentication = memberService.logIn(httpServletRequest, httpServletResponse, requestDto);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        memberService.logOut(httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("로그아웃 성공", HttpStatus.OK.value()));
    }

    // 전체 회원 조회(admin만 가능)
    @GetMapping("")
    public ResponseEntity<Page<MyInfoResponseDto>> findAllMember(Pageable pageable) {
        Page<MyInfoResponseDto> members = memberService.findAll(pageable);
        return ResponseEntity.ok().body(members);
    }

    // 특정 회원 조회(일반 회원이 자신의 상세정보 열람)
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = memberService.findById(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok().body(MyInfoResponseDto.of(member));
    }

    @GetMapping("/search") // 키워드 검색 회원 목록 조회
    public ResponseEntity<Page<MyInfoResponseDto>> searchMembers(MemberSearchCond cond,
        Pageable pageable) {
        Page<MyInfoResponseDto> result = memberService.searchMembers(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    // admin 권한일 경우 다른 회원의 상세정보 열람 가능, 일반회원은 자신의 정보만 열람 가능
    @GetMapping("/myinfo/{memberNo}")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long memberNo) {
        Member member;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        // admin 권한일 경우
        if (isAdmin) {
            member = memberService.findById(memberNo);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
        return ResponseEntity.ok().body(MyInfoResponseDto.of(member));
    }

    // 회원 수정
    @PutMapping("")
    public ResponseEntity<Member> updateMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @RequestBody MemberRequestDto memberRequestDto) {
        Member updatedMember = memberService.update(userDetails.getMember().getMemberNo(), memberRequestDto);
        return ResponseEntity.ok().body(updatedMember);
    }

    // 회원 권한 수정
    @PutMapping("/admin/u/{memberNo}")
    public ResponseEntity<?> updateMemberRole(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody MemberRequestDto memberRequestDto,
        @PathVariable Long memberNo) {
        Member member;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        // admin 권한일 경우
        if (isAdmin) {
            member = memberService.findById(memberNo);
            memberService.updateRole(member.getMemberNo(), memberRequestDto);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
        return ResponseEntity.ok().body(MemberResponseDto.of(member));
    }

    // 회원 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse> deleteMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        memberService.deleteMember(userDetails.getMember().getMemberNo(), userDetails.getMember().getMemberId(), httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // 회원 삭제(admin이 다른 회원 삭제)
    @DeleteMapping("/admin/r/{memberNo}")
    public ResponseEntity<?> deleteMemberAdmin(@AuthenticationPrincipal UserDetailsImpl userDetails,
        HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        @PathVariable Long memberNo) {
        Member member;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        // admin 권한일 경우
        if (isAdmin) {
            member = memberService.findById(memberNo);
            memberService.deleteMemberAdmin(member.getMemberNo(), member.getMemberId(), httpRequest, httpResponse);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // OAuth2 인증 성공 후 추가 정보 수정 실행(memberNickname)
    @PutMapping("/addinfo")
    public ResponseEntity<Member> addInfo(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody MemberNicknameRequestDto memberNicknameRequestDto) {
        Member updateOauth2Member = memberService.updateNickname(userDetails.getMember().getMemberNo(), memberNicknameRequestDto);
        log.info("userDetails : {}", userDetails);
        return ResponseEntity.ok().body(updateOauth2Member);
    }

    // 회원가입 시 인증번호 일치하는지 확인(검증)
    @PostMapping("/verifycode")
    public ResponseEntity<String> verifyCode(@RequestBody VerifyCodeRequestDto verifyCodeRequestDto) {
        if (memberService.verifyCode(verifyCodeRequestDto.getMemberEmail(), verifyCodeRequestDto.getCode())) {
            return ResponseEntity.ok(verifyCodeRequestDto.getCode());
        } else {
            return ResponseEntity.badRequest().body("인증 실패");
        }
    }

    // 아이디 찾기
    @PostMapping("/findid")
    public ResponseEntity<String> findMemberId(@RequestBody FindIdRequestDto findIdRequestDto) {
        String foundMemberId = memberService.findIdByNicknameEmail(
                findIdRequestDto.getMemberNickname(), findIdRequestDto.getMemberEmail(), findIdRequestDto.getCode());
        if (foundMemberId != null) {
            return ResponseEntity.ok()
                    .body("아이디 찾기 성공!, 찾은 아이디 : " + foundMemberId);
        } else {
            return ResponseEntity.badRequest().body("아이디 찾기 실패");
        }
    }

    // 비밀번호 변경
    @PutMapping("/changepw")
    public ResponseEntity<String> changeMemberPw(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestBody ChangePwRequestDto changePwRequestDto) {
        Optional<Member> optionalMember = memberRepository.findById(userDetails.getMember().getMemberNo());
        if (optionalMember.isPresent()) {
            if (memberService.changePassword(userDetails.getMember().getMemberNo(),
                    changePwRequestDto.getChangePw(), changePwRequestDto.getConfirmPw())) {
                return ResponseEntity.ok()
                        .body("비밀번호 변경 성공, 변경한 비밀번호 : " + changePwRequestDto.getChangePw());
            }
        }
        return ResponseEntity.badRequest().body("비밀번호 변경 실패");
    }

    // 회원가입 시 이메일 중복 확인
    @GetMapping("/checkemail")
    public ResponseEntity<ApiResponse> existsMemberEmail(String memberEmail) {
        if (memberService.existsByMemberEmail(memberEmail)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("이미 존재하는 이메일입니다", HttpStatus.BAD_REQUEST.value()));
        } else {
            return ResponseEntity.ok().body(new ApiResponse("사용가능한 이메일입니다", HttpStatus.OK.value()));
        }
    }

    // 회원가입 시 아이디 중복 확인
    @GetMapping("/checkid")
    public ResponseEntity<ApiResponse> existsMemberId(String memberId) {
        if (memberService.existsByMemberId(memberId)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("이미 존재하는 아이디입니다", HttpStatus.BAD_REQUEST.value()));
        } else {
            return ResponseEntity.ok().body(new ApiResponse("사용가능한 아이디입니다", HttpStatus.OK.value()));
        }
    }

    // 내정보 열람 시 본인 비밀번호 확인
//    @PostMapping("/myinfo/check")
//    public ResponseEntity<ApiResponse> verifyPassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
//                                                      HttpServletRequest request, HttpServletResponse response,
//                                                      String password) {
//        boolean isValid = memberService.checkPassword(request, response, password, userDetails.getMember().getMemberNo());
//        if (isValid) {
//            return ResponseEntity.ok()
//                    .body(new ApiResponse("성공", HttpStatus.OK.value()));
//        } else {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse("비밀번호가 틀립니다", HttpStatus.BAD_REQUEST.value()));
//        }
//    }

    @PostMapping("/myinfo/check")
    public ResponseEntity<ApiResponse> verifyPassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
        HttpServletRequest request, HttpServletResponse response,
        String password) {
        boolean isValid = memberService.checkPassword(request, response, password, userDetails.getMember().getMemberNo());
        if (isValid) {
            return ResponseEntity.ok()
                .body(new ApiResponse("성공", HttpStatus.OK.value()));
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

}
