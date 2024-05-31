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
    private String memberEmail;
    
    @NotBlank(message = "닉네임은 반드시 입력해주세요")
    private String memberNickname;

    @NotBlank(message = "비밀번호는 반드시 입력해주세요")
    private String memberPw;

    public MemberRequestDto(String modifiedMemberNickname, String modifiedMemberPw) {
        this.memberNickname = modifiedMemberNickname;
        this.memberPw = modifiedMemberPw;
    }

    public Member toEntity(PasswordEncoder passwordEncoder){
        return Member.builder()
                .memberId(this.memberId)
                .memberEmail(this.memberEmail)
                .memberNickname(this.memberNickname)
                .memberPw(passwordEncoder.encode(this.memberPw))
                .role(Role.MEMBER)
                .providerType(ProviderType.LOCAL)
                .build();
    }

}
