package com.market.domain.member.email;

import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.redis.RedisUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private static final int AUTH_CODE_LENGTH = 6;
    private static final int TEMP_PASSWORD_NUMBER_LENGTH = 6;
    private static final int TEMP_PASSWORD_LETTER_LENGTH = 3;
    private static final Duration REDIS_EXPIRATION = Duration.ofMinutes(3);
    private static final char[] SPECIAL_CHARS = {'!', '@', '#', '$', '%', '^', '&', '*'};

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
/*    public String sendMail(EmailMessageEntity emailMessageEntity, String type) {

        String authNum = createAuthNum();
        String tempPassword = createTempPassword();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        if (type.equals("password")) {
            Optional<Member> optionalMember = memberRepository.findByMemberEmail(
                emailMessageEntity.getTo());
            if (optionalMember.isEmpty()) {
                log.error("해당 이메일에 해당하는 회원을 찾을 수 없습니다: {}", emailMessageEntity.getTo());
                throw new RuntimeException("해당 이메일에 해당하는 회원을 찾을 수 없습니다");
            }
            memberService.SetTempPassword(emailMessageEntity.getTo(), tempPassword);
        }

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false,
                "UTF-8");
            mimeMessageHelper.setTo(emailMessageEntity.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessageEntity.getSubject()); // 메일 제목

            if (type.equals("password")) { // 임시비밀번호인 경우
                mimeMessageHelper.setText(setContext(tempPassword, type),
                    true); // 메일 본문 내용, HTML 여부
                javaMailSender.send(mimeMessage);
                // 임시비밀번호는 비교할 필요성이 없으므로 굳이 Redis 에 저장할 필요 없음
                // redisUtils.setValues(emailMessageEntity.getTo(), tempPassword, Duration.ofMinutes(3));
                return tempPassword;

            } else { // 인증번호인 경우
                mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
                javaMailSender.send(mimeMessage);
                redisUtils.setValues(emailMessageEntity.getTo(), authNum, Duration.ofMinutes(3));
                return authNum;
            }

        } catch (MessagingException e) {
            log.info("이메일 전송 실패");
            throw new RuntimeException(e);
        }
    }*/

    // 비동기 메일 전송
    @Async("asyncExecutor")
    public void sendMailAsync(EmailMessageEntity emailMessageEntity, String code, String type) {
        try {
            MimeMessage mimeMessage = createMimeMessage(emailMessageEntity, code, type);
            javaMailSender.send(mimeMessage);

            if (type.equals("email")) {
                redisUtils.setValues(emailMessageEntity.getTo(), code, REDIS_EXPIRATION);
            }

            log.info("이메일 전송 완료: {}", emailMessageEntity.getTo());
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    // 임시비밀번호 생성 및 메일 전송을 비동기로 처리
    @Async("asyncExecutor")
    @Transactional
    public void sendTempPasswordAsync(EmailMessageEntity emailMessageEntity) {
        try {
            validateMemberExists(emailMessageEntity.getTo());
            // 임시비밀번호 생성
            String tempPassword = createTempPassword();
            memberService.SetTempPassword(emailMessageEntity.getTo(), tempPassword);

            MimeMessage mimeMessage = createMimeMessage(emailMessageEntity, tempPassword, "password");
            javaMailSender.send(mimeMessage);

            log.info("임시 비밀번호 이메일 전송 완료: {}", emailMessageEntity.getTo());
        } catch (MessagingException e) {
            log.error("임시 비밀번호 이메일 전송 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    private MimeMessage createMimeMessage(EmailMessageEntity emailMessageEntity, String code, String type)
        throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        helper.setTo(emailMessageEntity.getTo());
        helper.setSubject(emailMessageEntity.getSubject());
        helper.setText(setContext(code, type), true);

        return mimeMessage;
    }

    @Transactional(readOnly = true)
    protected void validateMemberExists(String email) {
        memberRepository.findByMemberEmail(email)
            .orElseThrow(() -> {
                log.error("존재하지 않는 이메일입니다: {}", email);
                return new BusinessException(ErrorCode.NOT_FOUND_EMAIL);
            });
    }

    // 인증번호 생성 메서드
    public String createAuthNum() {
        return generateRandomNumbers(AUTH_CODE_LENGTH);
    }

    // 임시비밀번호 생성 메서드
    private String createTempPassword() {
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        // 특수문자 추가
        password.append(SPECIAL_CHARS[random.nextInt(SPECIAL_CHARS.length)]);
        // 숫자 추가
        password.append(generateRandomNumbers(TEMP_PASSWORD_NUMBER_LENGTH));
        // 영문자 추가
        for (int i = 0; i < TEMP_PASSWORD_LETTER_LENGTH; i++) {
            if (random.nextBoolean()) {
                password.append((char) (random.nextInt(26) + 65)); // A-Z
            } else {
                password.append((char) (random.nextInt(26) + 97)); // a-z
            }
        }
        // 문자열 섞기
        return shuffleString(password.toString());
    }

    private String generateRandomNumbers(int length) {
        Random random = new Random();
        StringBuilder numbers = new StringBuilder();
        for (int i = 0; i < length; i++) {
            numbers.append(random.nextInt(10));
        }
        return numbers.toString();
    }

    private String shuffleString(String input) {
        List<Character> characters = input.chars()
            .mapToObj(ch -> (char) ch)
            .collect(Collectors.toList());
        Collections.shuffle(characters);
        return characters.stream()
            .map(String::valueOf)
            .collect(Collectors.joining());
    }

    // 인증코드 추가된 HTML 적용
    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }
}
