package com.market.global.security.oauth2;

import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.withdrawMember.service.WithdrawMemberService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service                                  // DefaultOAuth2UserService : 리소스 서버에서 사용자 정보 받아오는 클래스
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WithdrawMemberService withdrawMemberService;

    private ProviderType providerType;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 유저 정보(attributes) 가져오기(DefaultOAuth2User의 attributes)
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        // 테스트, 지워도됨
        log.info("clientRegistration : " + userRequest.getClientRegistration());
        log.info("client id : " + userRequest.getClientRegistration().getClientId());
        log.info("client name : " + userRequest.getClientRegistration().getClientName());
        log.info("registration id : " + userRequest.getClientRegistration().getRegistrationId());
        log.info("accessToken(third-party) : " + userRequest.getAccessToken().getTokenValue());
        log.info("attributes : " + oAuth2UserAttributes);

        // 2. providerType 가져오기(third-party id)
        providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        // 3. userNameAttributeName 가져오기(yml에서 설정한 provider의 user-name-attribute값. google은 "sub".)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 유저 정보 dto 생성
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(providerType, oAuth2UserAttributes);

        // 5. 회원가입 및 로그인
        Member member = SaveOrUpdate(oAuth2UserInfo);

        // 6. OAuth2로 반환
        return new UserDetailsImpl(member, oAuth2UserAttributes);
    }

    private Member SaveOrUpdate(OAuth2UserInfo OAuth2UserInfo) {
        Optional<Member> optionalMember = memberRepository.findByMemberEmail(OAuth2UserInfo.memberEmail());
        // 로그인하려는 회원의 이메일이 탈퇴회원 DB에 있다면 예외 발생 (탈퇴 or 탈퇴 처리된 OAuth 회원 로그인 차단 위함)
        if (withdrawMemberService.existsMemberEmail(OAuth2UserInfo.memberEmail())) {
            throw new BusinessException(ErrorCode.EXISTS_WITHDRAWMEMBER_EMAIL);
        }

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            if (providerType != member.getProviderType()) {
                throw new BusinessException(ErrorCode.EXISTS_EMAIL);
            }
            return member;
        }
        return memberRepository.save(OAuth2UserInfo.toEntity(passwordEncoder));
    }

}
