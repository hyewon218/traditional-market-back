package com.market.domain.member.email;

import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.redis.RedisUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final MemberServiceImpl memberService;
    private final MemberRepository memberRepository;
    private final RedisUtils redisUtils;

    public String sendMail(EmailMessageEntity emailMessageEntity, String type) {

        String authNum = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        if (type.equals("password")) {
            Optional<Member> optionalMember = memberRepository.findByMemberEmail(emailMessageEntity.getTo());
            if (optionalMember.isEmpty()) {
                log.error("해당 이메일에 해당하는 회원을 찾을 수 없습니다: {}", emailMessageEntity.getTo());
                throw new RuntimeException("해당 이메일에 해당하는 회원을 찾을 수 없습니다");
            }
            memberService.SetTempPassword(emailMessageEntity.getTo(), authNum);
        }

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessageEntity.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessageEntity.getSubject()); // 메일 제목
            mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage);
            // redis에 저장
            redisUtils.setValues(emailMessageEntity.getTo(), authNum, Duration.ofMinutes(3));
            return authNum;

        } catch (MessagingException e) {
            log.info("실패");
            throw new RuntimeException(e);
        }
    }

    // 인증번호, 임시비밀번호 생성 메서드
    private String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for(int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97));
                    break;
                case 1: key.append((char) ((int) random.nextInt(26) + 95));
                    break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }

    // thymeleaf 통한 인증코드 추가된 HTML 적용
    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }
}
