package com.market.global.security.oauth2;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Builder
public record OAuth2UserInfo(
        String memberEmail,
        String memberNickname,
        String profile,
        ProviderType providerType
) {

    public static OAuth2UserInfo of(ProviderType providerType, Map<String, Object> attributes) {
        // providerType별로 userInfo 생성
        return switch (providerType) {
            case GOOGLE -> ofGoogle(providerType, attributes);
            case KAKAO -> ofKakao(providerType, attributes);
            case NAVER -> ofNaver(providerType, attributes);
            default -> throw new IllegalArgumentException("유효하지않은 provider type입니다");
        };
    }

    private static OAuth2UserInfo ofGoogle(ProviderType providerType, Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .memberEmail((String) attributes.get("email"))
                .memberNickname((String) attributes.get("name"))
                .providerType(providerType)
//                .profile((String) attributes.get("picture"))
                .build();
    }

    private static OAuth2UserInfo ofKakao(ProviderType providerType, Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
                .memberEmail((String) account.get("email")) // 카카오에서 추가 검증 필요
                .memberNickname((String) profile.get("nickname"))
                .providerType(providerType)
//                .profile((String) profile.get("profile_image_url"))
//                .birthday((String) account.get("birthday"))
                .build();
    }

    private static OAuth2UserInfo ofNaver(ProviderType providerType, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2UserInfo.builder()
                .memberEmail((String) response.get("email"))
                .memberNickname((String) response.get("nickname"))
                .providerType(providerType)
//                .memberName((String) response.get("name"))
//                .memberPhone(transformPhoneNumber((String) response.get("mobile")))
                .build();
    }

    public Member toEntity(PasswordEncoder passwordEncoder) {
        // 랜덤 memberId 생성
        String memberId = generateRandomId();
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        String randomTag = generateRandomTag();

        return Member.builder()
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberNickname(memberNickname)
                .randomTag(randomTag)
                .nicknameWithRandomTag(memberNickname + randomTag)
                .memberPw(encodedPassword)
                .role(Role.MEMBER)
                .providerType(providerType)
                .build();
    }

    // providerType에 따라 랜덤 아이디 생성
    private String generateRandomId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // providerType에 따른 시작 문자 설정
        switch (providerType) {
            case NAVER -> sb.append("N");
            case KAKAO -> sb.append("K");
            case GOOGLE -> sb.append("G");
            default -> throw new IllegalArgumentException("유효하지 않은 provider type입니다");
        }

        // 0~9 숫자와 영문 대소문자 중 랜덤하게 3개 추가
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 2; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        // # 추가
        sb.append("#");

        // 0~9 숫자와 영문 대소문자 중 랜덤하게 7개 추가
        for (int i = 0; i < 7; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    // 닉네임 뒤 #으로 시작하는 랜덤태그 생성
    private String generateRandomTag() {
        StringBuilder sb = new StringBuilder();
        sb.append("#"); // "#"으로 시작하도록 추가

        // 나머지 자리에는 0부터 9까지의 숫자를 랜덤하게 생성
        for (int i = 0; i < 7; i++) {
            int digit = (int) (Math.random() * 10);
            sb.append(digit);
        }
        return sb.toString();
    }

    // 휴대전화번호 DB에 저장될때 해당 형식으로 저장되도록 하는 메서드
    private static String transformPhoneNumber(String memberPhone) {
        // 입력된 휴대전화번호에서 "-" 문자를 제거
        String cleanedPhoneNumber = memberPhone.replaceAll("-", "");
        return cleanedPhoneNumber;
    }
}
