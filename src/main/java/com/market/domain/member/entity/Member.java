package com.market.domain.member.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.cart.entity.Cart;
import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.order.entity.Order;
import com.market.domain.shop.entity.Shop;
import com.market.global.security.oauth2.ProviderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private Role role;  // member, seller, manager, admin 권한

    @Enumerated(EnumType.STRING)
    private ProviderType providerType; // Google, Naver, Kakao, Local 로그인

    @Column(name = "last_nickname_change_date")
    private LocalDateTime lastNicknameChangeDate; // 마지막 닉네임 변경 시간

    private boolean isWarning; // 제재 받은 상태인지 여부 (댓글, 채팅상담 제한)

    @Column(name = "warning_start_date")
    private LocalDateTime warningStartDate; // 제재 시작 시간

    private Long countWarning; // 제재 누적 횟수 (제재 풀릴때 +1 증가함)

    private Long countReport; // 다른 회원에게 신고 당한 횟수, 10회마다 isWarning true로 변환

    @Builder.Default
    private List<String> reportMember = new ArrayList<>(); // 하루동안 신고한 회원 목록 (같은 회원에 대해서 하루에 한번만 신고 가능)
    
    @Builder.Default
    private List<String> reporters = new ArrayList<>(); // 나를 신고한 회원 목록 (관리자만 조회 가능)

    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "cart_no")
    private Cart cart;

    @Builder.Default
    @OneToMany(mappedBy = "seller", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Shop> shops = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    public Member(String memberId, String memberEmail, String memberNickname, String memberPw, String nicknameWithRandomTag, ProviderType providerType, Role role) {
        this.memberId = memberId;
        this.memberEmail = memberEmail;
        this.memberNickname = memberNickname;
        this.memberPw = memberPw;
        this.nicknameWithRandomTag = nicknameWithRandomTag;
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
    
    // 닉네임 변경
    public void updateNickname(String memberNickname) {
        this.memberNickname = memberNickname;
        this.nicknameWithRandomTag = memberNickname + this.randomTag;
        this.lastNicknameChangeDate = LocalDateTime.now();
    }
    
    // OAuth2.0 최초 로그인 시 추가정보 업데이트
    public void updateOAuthInfo(String memberNickname) {
        this.memberNickname = memberNickname;
        this.nicknameWithRandomTag = memberNickname + this.randomTag;
    }

    // 제재 시작 (isWarning(제재 여부) true로 변경)
    public void setIsWarning(boolean isWarning) {
        this.isWarning = isWarning;
    }
    
    // 제재 시작 시간
    public void setWarningTime(LocalDateTime now) {
        this.warningStartDate = now;
    }

    // 제재 누적 횟수 증가
    public void setCountWarning(Long countWarning) {
        this.countWarning = countWarning + 1;
    }

    // 다른 회원에게 신고 당한 횟수
    public void setCountReport() {
        this.countReport++;
    }
    
    // 신고 목록 저장
    public void setReportMember(List<String> reportMember) {
        this.reportMember = reportMember;
    }

    // 나를 신고한 회원 목록 저장
    public void setReporters(List<String> reporters) {
        this.reporters = reporters;
    }

    // OAuth2 로그인 시 해당 사이트의 email과 다르다면 해당 사이트 이메일로 업데이트
    public void updateEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }
}
