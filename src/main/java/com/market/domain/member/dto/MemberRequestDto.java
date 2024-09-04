package com.market.domain.member.dto;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.global.security.oauth2.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {

    @NotBlank(message = "아이디는 반드시 입력해주세요")
    @Size(max = 15, message = "아이디는 최대 15자까지 입력할 수 있습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "아이디는 영문과 숫자만 포함할 수 있습니다.")
    private String memberId;

    @NotBlank(message = "이메일은 반드시 입력해주세요")
    private String memberEmail; // 아래 emailId와 domainCustom 합쳐서 저장, 프론트에서 처리

    private String emailId;
    private String domainCustom;

    private String memberNickname;
    private String randomTag; // # + 0~9까지 랜덤으로 생성되는 숫자 7자리
    private String nicknameWithRandomTag; // 닉네임 + 랜덤태그, ex) 닉네임#1234567

    @NotBlank(message = "비밀번호는 반드시 입력해주세요")
    @Size(min = 8, max = 16, message = "비밀번호는 최소 8자, 최대 16자까지 입력할 수 있습니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@!#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$", message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String memberPw;

    private String confirmPw;

    private Role role;

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
                .isWarning(false)
                .countWarning(0L)
                .countReport(0L)
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