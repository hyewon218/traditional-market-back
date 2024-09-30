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
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final MemberServiceImpl memberService;
    private final MemberRepository memberRepository;
    private final RedisUtils redisUtils;

//    public String sendMail(EmailMessageEntity emailMessageEntity, String type) {
//
//        String authNum = createCode();
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//        if (type.equals("password")) {
//            Optional<Member> optionalMember = memberRepository.findByMemberEmail(emailMessageEntity.getTo());
//            if (optionalMember.isEmpty()) {
//                log.error("해당 이메일에 해당하는 회원을 찾을 수 없습니다: {}", emailMessageEntity.getTo());
//                throw new RuntimeException("해당 이메일에 해당하는 회원을 찾을 수 없습니다");
//            }
//            memberService.SetTempPassword(emailMessageEntity.getTo(), authNum);
//        }
//
//        try {
//            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//            mimeMessageHelper.setTo(emailMessageEntity.getTo()); // 메일 수신자
//            mimeMessageHelper.setSubject(emailMessageEntity.getSubject()); // 메일 제목
//            mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
//            javaMailSender.send(mimeMessage);
//            // redis에 저장
//            redisUtils.setValues(emailMessageEntity.getTo(), authNum, Duration.ofMinutes(3));
//            return authNum;
//
//        } catch (MessagingException e) {
//            log.info("실패");
//            throw new RuntimeException(e);
//        }
//    }
    
    // 인증번호, 임시비밀번호 생성 독립적으로 처리
    public String sendMail(EmailMessageEntity emailMessageEntity, String type) {

        String authNum = createAuthNum();
        String tempPassword = createTempPassword();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        if (type.equals("password")) {
            Optional<Member> optionalMember = memberRepository.findByMemberEmail(emailMessageEntity.getTo());
            if (optionalMember.isEmpty()) {
                log.error("해당 이메일에 해당하는 회원을 찾을 수 없습니다: {}", emailMessageEntity.getTo());
                throw new RuntimeException("해당 이메일에 해당하는 회원을 찾을 수 없습니다");
            }
            memberService.SetTempPassword(emailMessageEntity.getTo(), tempPassword);
        }

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessageEntity.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessageEntity.getSubject()); // 메일 제목

            if (type.equals("password")) { // 임시비밀번호인 경우
                mimeMessageHelper.setText(setContext(tempPassword, type), true); // 메일 본문 내용, HTML 여부
                javaMailSender.send(mimeMessage);
                redisUtils.setValues(emailMessageEntity.getTo(), tempPassword, Duration.ofMinutes(3));
                return tempPassword;

            } else { // 인증번호인 경우
                mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
                javaMailSender.send(mimeMessage);
                redisUtils.setValues(emailMessageEntity.getTo(), authNum, Duration.ofMinutes(3));
                return authNum;
            }

        } catch (MessagingException e) {
            log.info("실패");
            throw new RuntimeException(e);
        }
    }
    
    // 인증번호 생성 메서드
    private String createAuthNum() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            key.append(random.nextInt(10));
        }
        return key.toString();
    }
    
    // 임시비밀번호 생성 메서드
    private String createTempPassword() {
        Random random = new Random();

        // 특수문자 배열
        char[] specialChars = {'!', '@', '#', '$', '%', '^', '&', '*'};

        // 임시 비밀번호를 저장할 StringBuilder
        StringBuilder password = new StringBuilder();

        // 1. 특수문자 1개 추가
        password.append(specialChars[random.nextInt(specialChars.length)]);

        // 2. 숫자 6개 추가
        for (int i = 0; i < 6; i++) {
            password.append(random.nextInt(10));  // 0부터 9까지의 숫자 추가
        }

        // 3. 영어 대소문자 3개 추가
        for (int i = 0; i < 3; i++) {
            // 대문자(65~90) 또는 소문자(97~122) 중 하나를 무작위로 추가
            if (random.nextBoolean()) {
                password.append((char) (random.nextInt(26) + 65));  // A-Z
            } else {
                password.append((char) (random.nextInt(26) + 97));  // a-z
            }
        }

        // 4. 비밀번호의 순서를 섞기 (Collections.shuffle을 사용하기 위해 리스트로 변환)
        List<Character> passwordList = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            passwordList.add(c);
        }

        // 리스트를 섞은 후 다시 문자열로 변환
        Collections.shuffle(passwordList);
        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : passwordList) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }


    // thymeleaf 통한 인증코드 추가된 HTML 적용
    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }
}
