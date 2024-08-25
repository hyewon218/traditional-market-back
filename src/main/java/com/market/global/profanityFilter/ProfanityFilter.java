package com.market.global.profanityFilter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 비속어 필터 (아래는 적용되는 곳)
 * 회원가입 시 닉네임
 * 댓글
 * 문의하기
 * 채팅상담
 */
public class ProfanityFilter {
    // 비속어 리스트 정의
    private static final List<String> PROFANITY_LIST = Arrays.asList(
        "비속어"
        // 추가 비속어를 여기에 추가
    );

    // 비속어 검출 메서드
    public static boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String regex = String.join("|", PROFANITY_LIST);
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }
}
