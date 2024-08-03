package com.market.domain.member.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.cart.entity.Cart;
import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.order.entity.Order;
import com.market.global.security.oauth2.ProviderType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@Setter // 임시비밀번호 설정때문에 import
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no")
    private Long memberNo;

    @Column(nullable = false, unique = true)
    private String memberId;

    @Column(nullable = false, unique = true)
    private String memberEmail;

    private String memberNickname;

    private String randomTag;

    @Column(nullable = false)
    private String nicknameWithRandomTag;

    @Column(nullable = false)
    private String memberPw;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Role role;  // member, seller, admin 권한

    @Enumerated(EnumType.STRING)
    private ProviderType providerType; // Google, Naver, Kakao, Local 로그인

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_no")
    private Cart cart;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Order> orders = new ArrayList<>();

    public Member(String memberId, String memberEmail, String memberNickname, String memberPw, ProviderType providerType, Role role) {
        this.memberId = memberId;
        this.memberEmail = memberEmail;
        this.memberNickname = memberNickname;
        this.memberPw = memberPw;
        this.providerType = providerType;
        this.role = role;
    }

    // 회원 정보 수정 메서드(닉네임, 비밀번호 수정)
    public void update(MemberRequestDto requestDto) {
        this.memberNickname = requestDto.getMemberNickname();
        this.nicknameWithRandomTag = requestDto.getMemberNickname() + this.randomTag;
        this.memberPw = requestDto.getMemberPw();
        this.role = requestDto.getRole();
    }

    // 회원 권한 수정 메서드(admin만 가능)
    public void updateRole(MemberRequestDto requestDto) {
        this.role = requestDto.getRole();
    }

    public void updateNickname(String memberNickname) {
        this.memberNickname = memberNickname;
        this.nicknameWithRandomTag = memberNickname + this.randomTag;
    }

    // OAuth2 로그인 시 해당 사이트의 email과 다르다면 해당 사이트 이메일로 업데이트
    public void updateEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }
}
