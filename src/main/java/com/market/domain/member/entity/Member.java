package com.market.domain.member.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.cart.entity.Cart;
import com.market.domain.member.constant.Role;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
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

    @Column(nullable = false)
    private String memberPw;

    @Column(nullable = false, unique = true)
    private String memberEmail;

    @Enumerated(EnumType.STRING)
    private Role role;  // member, admin 권한

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_no")
    private Cart cart;

    // 회원 정보 수정 메서드
    public void update(String memberId, String memberPw) {
        this.memberId = memberId;
        this.memberPw = memberPw;
    }

}
