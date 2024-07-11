package com.market.domain.member.dto;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.global.security.oauth2.ProviderType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {

    @NotBlank(message = "아이디는 반드시 입력해주세요")
    private String memberId;

    @NotBlank(message = "이메일은 반드시 입력해주세요")
    private String memberEmail; // 아래 emailId와 domainCustom 합쳐서 저장, 프론트에서 처리

    private String emailId;
    private String domainCustom;

    private String memberNickname;
    private String randomTag; // # + 0~9까지 랜덤으로 생성되는 숫자 7자리
    private String nicknameWithRandomTag; // 닉네임 + 랜덤태그, ex) 닉네임#1234567

    @NotBlank(message = "비밀번호는 반드시 입력해주세요")
    private String memberPw;

    @NotBlank(message = "비밀번호 확인을 반드시 입력해주세요")
    private String confirmPw;

    // 테스트에서만 사용함
    public MemberRequestDto(String modifiedMemberNickname, String modifiedMemberPw) {
        this.memberNickname = modifiedMemberNickname;
        this.memberPw = modifiedMemberPw;
    }

    // 테스트에서만 사용함
    public MemberRequestDto(String memberId, String memberEmail, String memberPw) {
        this.memberId = memberId;
        this.memberEmail = memberEmail;
        this.memberPw = memberPw;
    }

    public Member toEntity(PasswordEncoder passwordEncoder){
        String randomTag = generateRandomTag();

        return Member.builder()
                .memberId(this.memberId)
                .memberEmail(this.memberEmail)
                .memberNickname(this.memberNickname)
                .randomTag(randomTag)
                .nicknameWithRandomTag(this.memberNickname + randomTag)
                .memberPw(passwordEncoder.encode(this.memberPw))
                .role(Role.MEMBER)
                .providerType(ProviderType.LOCAL)
                .build();
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
}