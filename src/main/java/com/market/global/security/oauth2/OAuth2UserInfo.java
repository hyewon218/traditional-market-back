package com.market.global.security.oauth2;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
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
        String memberId = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        return Member.builder()
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberNickname(memberNickname)
                .memberPw(encodedPassword)
                .providerType(providerType)
                .role(Role.MEMBER)
                .build();
    }

    // 휴대전화번호 DB에 저장될때 해당 형식으로 저장되도록 하는 메서드
    private static String transformPhoneNumber(String memberPhone) {
        // 입력된 휴대전화번호에서 "-" 문자를 제거
        String cleanedPhoneNumber = memberPhone.replaceAll("-", "");
        return cleanedPhoneNumber;
    }
}
