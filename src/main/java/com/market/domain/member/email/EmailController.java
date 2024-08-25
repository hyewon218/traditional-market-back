package com.market.domain.member.email;

import com.market.domain.member.dto.FindIdRequestDto;
import com.market.domain.member.dto.FindPwRequestDto;
import com.market.domain.member.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
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
    private final MemberServiceImpl memberService;

    // 회원가입 시 '인증번호 전송' 버튼
    @PostMapping("/email")
    public ResponseEntity<EmailResponseDto> sendMail(@RequestBody EmailPostDto emailPostDto) {
        EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
                .to(emailPostDto.getMemberEmail())
                .subject("<<<SWC_전통시장테스트>>> 이메일 인증을 위한 인증코드 전송")
                .build();

        String code = emailService.sendMail(emailMessageEntity, "email");
        EmailResponseDto emailResponseDto = new EmailResponseDto();
        emailResponseDto.setCode(code);
        return ResponseEntity.ok().body(emailResponseDto);
    }

    // 아이디 찾기 시 '인증번호 전송' 버튼
    @PostMapping("/email/findid")
    public ResponseEntity<?> sendMailForFindId(@RequestBody FindIdRequestDto findIdRequestDto) {
        boolean isMemberExist = memberService.findMemberByEmail(findIdRequestDto.getMemberEmail());

        if (isMemberExist) {
            EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
                    .to(findIdRequestDto.getMemberEmail())
                    .subject("<<<우리동네 전통시장>>> 아이디 찾기를 위한 인증코드 발급")
                    .build();

            String code = emailService.sendMail(emailMessageEntity, "email");
            EmailResponseDto emailResponseDto = new EmailResponseDto();
            emailResponseDto.setCode(code);
            return ResponseEntity.ok().body(emailResponseDto);
        } else {
            return ResponseEntity.badRequest().body("이메일에 해당하는 회원이 존재하지않습니다");
        }
    }

    // 비밀번호 찾기 시 '임시비밀번호 발급' 버튼
    @PostMapping("/email/temppw")
    public ResponseEntity<String> sendMailForFindPw(@RequestBody FindPwRequestDto findPwRequestDto) {
        boolean isMemberExist = memberService.findMemberByIdAndEmail(
                findPwRequestDto.getMemberId(), findPwRequestDto.getMemberEmail());

        if (isMemberExist) {
            EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
                    .to(findPwRequestDto.getMemberEmail())
                    .subject("<<<우리동네 전통시장>>> 임시비밀번호 발급")
                    .build();

            String tempPassword = emailService.sendMail(emailMessageEntity, "password");
            return ResponseEntity.ok().body(tempPassword);
        } else {
            return ResponseEntity.badRequest().body("아이디와 이메일에 해당하는 회원이 존재하지않습니다");
        }
    }

}
