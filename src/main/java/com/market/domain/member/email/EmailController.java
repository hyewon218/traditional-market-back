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

    // 회원가입 시 '인증번호 전송' 버튼
//    @PostMapping("/email")
//    public ResponseEntity<EmailResponseDto> sendMail(@RequestBody EmailPostDto emailPostDto) {
//        EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
//                .to(emailPostDto.getMemberEmail())
//                .subject("<<<SWC_전통시장테스트>>> 이메일 인증을 위한 인증코드 전송")
//                .build();
//
//        String code = emailService.sendMail(emailMessageEntity, "email");
//        EmailResponseDto emailResponseDto = new EmailResponseDto();
//        emailResponseDto.setCode(code);
//        return ResponseEntity.ok().body(emailResponseDto);
//    }

    // 비동기, 회원가입 시 '인증번호 전송' 버튼
    @PostMapping("/email")
    public ResponseEntity<EmailResponseDto> sendMail(@RequestBody EmailPostDto emailPostDto) {
        EmailMessageEntity emailMessageEntity = createEmailMessage(
            emailPostDto.getMemberEmail(),
            "<<<SWC_전통시장테스트>>> 이메일 인증을 위한 인증코드 전송"
        );
        // 인증 코드 생성
        String code = emailService.createAuthNum();
        // 이메일 발송을 비동기로 처리
        emailService.sendMailAsync(emailMessageEntity, code, "email");
        // 생성된 인증 코드 즉시 반환
        return ResponseEntity.ok(createEmailResponseDto(code));
    }

    // 아이디 찾기 시 '인증번호 전송' 버튼
//    @PostMapping("/email/findid")
//    public ResponseEntity<?> sendMailForFindId(@RequestBody FindIdRequestDto findIdRequestDto) {
//        boolean isMemberExist = memberService.findMemberByEmail(findIdRequestDto.getMemberEmail());
//
//        if (isMemberExist) {
//            EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
//                    .to(findIdRequestDto.getMemberEmail())
//                    .subject("<<<우리동네 전통시장>>> 아이디 찾기를 위한 인증코드 발급")
//                    .build();
//
//            String code = emailService.sendMail(emailMessageEntity, "email");
//            EmailResponseDto emailResponseDto = new EmailResponseDto();
//            emailResponseDto.setCode(code);
//            return ResponseEntity.ok().body(emailResponseDto);
//        } else {
//            return ResponseEntity.badRequest().body("이메일에 해당하는 회원이 존재하지않습니다");
//        }
//    }

    // 비동기, 아이디 찾기 시 '인증번호 전송' 버튼
    // 이후엔 휴대전화번호도 추가해 이메일, 휴대전화번호로 교차인증하기
    @PostMapping("/email/findid")
    public ResponseEntity<EmailResponseDto> sendMailForFindId(
        @RequestBody FindIdRequestDto findIdRequestDto) {
        String memberEmail = findIdRequestDto.getMemberEmail();
        // 존재하는 이메일인지 확인
        emailService.validateMemberExists(memberEmail);
        EmailMessageEntity emailMessageEntity = createEmailMessage(
            memberEmail,
            "<<<우리동네 전통시장>>> 아이디 찾기를 위한 인증코드 발급"
        );
        // 인증 코드 생성
        String code = emailService.createAuthNum();
        // 이메일 발송을 비동기로 처리
        emailService.sendMailAsync(emailMessageEntity, code, "email");
        // 생성된 인증 코드 즉시 반환
        return ResponseEntity.ok(createEmailResponseDto(code));
    }

    // 비밀번호 찾기 시 '임시비밀번호 발급' 버튼
//    @PostMapping("/email/temppw")
//    public ResponseEntity<String> sendMailForFindPw(@RequestBody FindPwRequestDto findPwRequestDto) {
//        boolean isMemberExist = memberService.findMemberByIdAndEmail(
//                findPwRequestDto.getMemberId(), findPwRequestDto.getMemberEmail());
//
//        if (isMemberExist) {
//            EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
//                    .to(findPwRequestDto.getMemberEmail())
//                    .subject("<<<우리동네 전통시장>>> 임시비밀번호 발급")
//                    .build();
//
//            String tempPassword = emailService.sendMail(emailMessageEntity, "password");
//            return ResponseEntity.ok().body(tempPassword);
//        } else {
//            return ResponseEntity.badRequest().body("아이디와 이메일에 해당하는 회원이 존재하지않습니다");
//        }
//    }

    // 비동기, 비밀번호 찾기 시 '임시비밀번호 발급' 버튼
    @PostMapping("/email/temppw")
    public ResponseEntity<ApiResponse> sendMailForFindPw(
        @RequestBody FindPwRequestDto findPwRequestDto) {

        EmailMessageEntity emailMessageEntity = createEmailMessage(
            findPwRequestDto.getMemberEmail(),
            "<<<우리동네 전통시장>>> 임시비밀번호 발급"
        );

        emailService.sendTempPasswordAsync(emailMessageEntity);

        return ResponseEntity.ok(
            new ApiResponse("임시 비밀번호가 이메일로 발송되었습니다.", HttpStatus.OK.value())
        );

    }

    // 이메일 메시지 생성 메서드
    private EmailMessageEntity createEmailMessage(String email, String subject) {
        return EmailMessageEntity.builder()
            .to(email)
            .subject(subject)
            .build();
    }

    // 이메일 응답 DTO 생성 메서드
    private EmailResponseDto createEmailResponseDto(String code) {
        EmailResponseDto emailResponseDto = new EmailResponseDto();
        emailResponseDto.setCode(code);
        return emailResponseDto;
    }
}
