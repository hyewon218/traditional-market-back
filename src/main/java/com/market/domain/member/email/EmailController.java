package com.market.domain.member.email;

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

    @PostMapping("/email")
    public ResponseEntity sendMail(@RequestBody EmailPostDto emailPostDto) {
        EmailMessageEntity emailMessageEntity = EmailMessageEntity.builder()
                .to(emailPostDto.getMemberEmail())
                .subject("<<<SWC_전통시장테스트>>> 이메일 인증을 위한 인증코드 전송")
                .build();

        String code = emailService.sendMail(emailMessageEntity, "email");
        EmailResponseDto emailResponseDto = new EmailResponseDto();
        emailResponseDto.setCode(code);
        return ResponseEntity.ok()
                .body(emailResponseDto);
    }

}
