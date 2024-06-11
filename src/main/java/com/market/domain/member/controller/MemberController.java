package com.market.domain.member.controller;

import com.market.domain.member.dto.*;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMember);
    }
    
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> logIn(HttpServletRequest httpServletRequest,
                                             HttpServletResponse httpServletResponse,
                                             @RequestBody MemberRequestDto requestDto) throws Exception {

        memberService.logIn(httpServletRequest, httpServletResponse, requestDto);
        return ResponseEntity.ok().body(new ApiResponse("로그인 성공", HttpStatus.OK.value()));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        memberService.logOut(httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("로그아웃 성공", HttpStatus.OK.value()));
    }

    // 전체 회원 조회
    @GetMapping("")
    public ResponseEntity<List<MemberResponseDto>> findAllMember() {

        List<MemberResponseDto> members = memberService.findAll()
                .stream()
                .map(MemberResponseDto::of) // 생성자 사용할때는 of 대신 new
                .toList();

        return ResponseEntity.ok()
                .body(members);
    }

    // 특정 회원 조회
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = memberService.findById(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok()
                .body(MemberResponseDto.of(member));
    }

    // admin 권한일 경우 다른 회원의 상세정보 열람 가능, 일반회원은 자신의 정보만 열람 가능
//    @GetMapping("/myinfo/{memberNo}")
//    public ResponseEntity<?> myInfo(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long memberNo) {
//        Member member;
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
//        // admin 권한일 경우
//        if (isAdmin) {
//            member = memberService.findById(memberNo);
//        // 로그인중인 사용자가 자신의 정보를 열람할 경우
//        } else if (userDetails.getMember().getMemberNo().equals(memberNo)) {
//            member = memberService.findById(userDetails.getMember().getMemberNo());
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("접근이 거부되었습니다");
//        }
//        return ResponseEntity.ok()
//                .body(new MemberResponseDto(member));
//    }

    // 회원 수정
    @PutMapping("")
    public ResponseEntity<Member> updateMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @RequestBody MemberRequestDto memberRequestDto) {
        Member updatedMember = memberService.update(userDetails.getMember().getMemberNo(), memberRequestDto);
        return ResponseEntity.ok()
                .body(updatedMember);
    }

    // 회원 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse> deleteMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        memberService.deleteMember(userDetails.getMember().getMemberNo(), userDetails.getMember().getMemberId(), httpRequest, httpResponse);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // OAuth2 인증 성공 후 추가 정보 수정 실행(memberNickname)
    @PutMapping("/addinfo")
    public ResponseEntity<Member> addInfo(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody MemberNicknameRequestDto memberNicknameRequestDto) {
        Member updateOauth2Member = memberService.updateNickname(userDetails.getMember().getMemberNo(), memberNicknameRequestDto);
        log.info("userDetails : {}", userDetails);
        return ResponseEntity.ok()
                .body(updateOauth2Member);
    }

    // 회원가입 시 인증번호 일치하는지 확인(검증)
    @PostMapping("/verifycode")
    public ResponseEntity<String> verifyCode(@RequestBody VerifyCodeRequestDto verifyCodeRequestDto) {
        if (memberService.verifyCode(verifyCodeRequestDto.getMemberEmail(), verifyCodeRequestDto.getCode())) {
            return ResponseEntity.ok("인증 성공");
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
    @PostMapping("/changepw")
    public ResponseEntity<String> changeMemberPw(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestBody ChangePwRequestDto changePwRequestDto) {
        Optional<Member> optionalMember = memberRepository.findById(userDetails.getMember().getMemberNo());
        if (optionalMember.isPresent()) {
            if (memberService.changePassword(userDetails.getMember().getMemberNo(), changePwRequestDto.getCurrentPw(),
                    changePwRequestDto.getChangePw(), changePwRequestDto.getConfirmPw())) {
                return ResponseEntity.ok()
                        .body("비밀번호 변경 성공, 변경한 비밀번호 : " + changePwRequestDto.getChangePw());
            }
        }
        return ResponseEntity.badRequest().body("비밀번호 변경 실패");
    }
}
