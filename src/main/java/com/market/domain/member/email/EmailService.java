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


    @Async("asyncExecutor") // 회원가입 및 아이디 찾기 시 메일 전송을 비동기로 처리
    public void sendAuthenticationMailAsync(EmailMessageEntity emailMessageEntity, String type) {
        // 인증번호 생성
        String authNum = createAuthNum();
        try {
            MimeMessage mimeMessage = createMimeMessage(emailMessageEntity, authNum, type);
            javaMailSender.send(mimeMessage); // 외부 시스템(메일 서버)과 통신
            // Redis 에 인증번호 저장
            redisUtils.setValues(emailMessageEntity.getTo(), authNum, REDIS_EXPIRATION);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    @Async("asyncExecutor") // 비밀번호 찾기 시 임시비밀번호 생성 및 메일 전송을 비동기로 처리
    @Transactional
    public void sendTemporaryPasswordMailAsync(String memberId,
        EmailMessageEntity emailMessageEntity,
        String type) {
        // 회원 존재 여부 확인
        boolean isMemberExist = memberService.findMemberByIdAndEmail(memberId,
            emailMessageEntity.getTo());
        if (!isMemberExist) {
            throw new BusinessException(ErrorCode.NOT_FOUND_MEMBER);
        }
        // 임시비밀번호 생성
        String tempPassword = createTempPassword();
        // 임시 비밀번호로 업데이트
        memberService.SetTempPassword(emailMessageEntity.getTo(), tempPassword);
        try {
            MimeMessage mimeMessage = createMimeMessage(emailMessageEntity, tempPassword, type);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    private MimeMessage createMimeMessage(EmailMessageEntity emailMessageEntity, String code,
        String type)
        throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        helper.setTo(emailMessageEntity.getTo());
        helper.setSubject(emailMessageEntity.getSubject());
        helper.setText(setContext(code, type), true);

        return mimeMessage;
    }

    // 인증코드 추가된 HTML 적용
    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }

    // 인증번호 생성
    private String createAuthNum() {
        return generateRandomNumbers(AUTH_CODE_LENGTH);
    }

    private String generateRandomNumbers(int length) {
        Random random = new Random();
        StringBuilder numbers = new StringBuilder();

        for (int i = 0; i < length; i++) {
            numbers.append(random.nextInt(10)); // 0부터 9까지의 숫자 추가
        }
        return numbers.toString();
    }

    // 임시비밀번호 생성
    private String createTempPassword() {
        Random random = new Random();
        // 임시 비밀번호를 저장할 StringBuilder
        StringBuilder password = new StringBuilder();
        // 특수문자 1개 추가
        password.append(SPECIAL_CHARS[random.nextInt(SPECIAL_CHARS.length)]);
        // 숫자 6개 추가
        password.append(generateRandomNumbers(TEMP_PASSWORD_NUMBER_LENGTH));
        // 영어 대소문자 3개 추가
        for (int i = 0; i < TEMP_PASSWORD_LETTER_LENGTH; i++) {
            // 대문자(65~90) 또는 소문자(97~122) 중 하나를 무작위로 추가
            if (random.nextBoolean()) {
                password.append((char) (random.nextInt(26) + 65)); // A-Z
            } else {
                password.append((char) (random.nextInt(26) + 97)); // a-z
            }
        }
        // 문자열 섞기
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        // 문자열을 문자 리스트로 변환
        List<Character> characters = input.chars()
            .mapToObj(ch -> (char) ch)
            .collect(Collectors.toList());
        // 리스트 섞기
        Collections.shuffle(characters);
        // 섞인 문자들을 다시 문자열로 변환
        return characters.stream()
            .map(String::valueOf)
            .collect(Collectors.joining());
    }

    @Transactional(readOnly = true)
    protected void validateMemberExists(String email) {
        memberRepository.findByMemberEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_EMAIL));
    }
}
