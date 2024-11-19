package com.market.domain.member.email;

import com.market.domain.member.dto.FindIdRequestDto;
import com.market.domain.member.dto.FindPwRequestDto;
import com.market.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/send-mail")
public class EmailController {

    private final EmailService emailService;

    // 비동기, 회원가입 시 '인증번호 전송' 버튼
    @PostMapping("/email")
    public ResponseEntity<ApiResponse> sendMail(@RequestBody EmailPostDto emailPostDto) {
        EmailMessageEntity emailMessageEntity = createEmailMessage(
            emailPostDto.getMemberEmail(),
            "<<<우리동네 전통시장>>> 이메일 인증을 위한 인증코드 전송"
        );
        emailService.sendAuthenticationMailAsync(emailMessageEntity, "email");
        return ResponseEntity.ok(
            new ApiResponse("인증번호가 이메일로 발송되었습니다.", HttpStatus.OK.value())
        );
    }

    // 비동기, 아이디 찾기 시 '인증번호 전송' 버튼
    // 이후엔 휴대전화번호도 추가해 이메일, 휴대전화번호로 교차인증하기
    @PostMapping("/email/findid")
    public ResponseEntity<ApiResponse> sendMailForFindId(
        @RequestBody FindIdRequestDto findIdRequestDto) {
        String memberEmail = findIdRequestDto.getMemberEmail();
        // 존재하는 회원인지 확인
        emailService.validateMemberExists(memberEmail);
        EmailMessageEntity emailMessageEntity = createEmailMessage(
            memberEmail,
            "<<<우리동네 전통시장>>> 아이디 찾기를 위한 인증코드 발급"
        );
        emailService.sendAuthenticationMailAsync(emailMessageEntity, "email");
        return ResponseEntity.ok(
            new ApiResponse("인증번호가 이메일로 발송되었습니다.", HttpStatus.OK.value())
        );
    }

    // 비동기, 비밀번호 찾기 시 '임시비밀번호 발급' 버튼
    @PostMapping("/email/temppw")
    public ResponseEntity<ApiResponse> sendMailForFindPw(
        @RequestBody FindPwRequestDto findPwRequestDto) {
        String memberEmail = findPwRequestDto.getMemberEmail();
        EmailMessageEntity emailMessageEntity = createEmailMessage(
            memberEmail,
            "<<<우리동네 전통시장>>> 임시비밀번호 발급"
        );
        emailService.sendTemporaryPasswordMailAsync(findPwRequestDto.getMemberId(),
            emailMessageEntity, "password"
        );
        return ResponseEntity.ok(
            new ApiResponse("임시 비밀번호가 이메일로 발송되었습니다.", HttpStatus.OK.value())
        );
    }

    // 이메일 메시지 생성
    private EmailMessageEntity createEmailMessage(String email, String subject) {
        return EmailMessageEntity.builder()
            .to(email)
            .subject(subject)
            .build();
    }
}
