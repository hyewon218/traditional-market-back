package com.market.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.delivery.repository.DeliveryRepository;
import com.market.domain.deliveryMessage.repository.DeliveryMessageRepository;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.MemberNicknameRequestDto;
import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.dto.MyInfoResponseDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.redis.RedisUtils;
import com.market.global.security.CookieUtil;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenProvider tokenProvider;
    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;
    private final DeliveryRepository deliveryRepository;
    private final InquiryRepository inquiryRepository;
    private final DeliveryMessageRepository deliveryMessageRepository;

    // 회원 생성
    @Override
    @Transactional
    public Member createMember(MemberRequestDto memberRequestDto){
        return memberRepository.save(memberRequestDto.toEntity(passwordEncoder));
    }

    // 회원 생성(아이디, 이메일 마스킹 처리)
//    @Override
//    public Member createMember(MemberRequestDto memberRequestDto) {
//        String maskedId = idMasking(memberRequestDto.getMemberId());
//        String maskedEmail = emailMasking(memberRequestDto.getMemberEmail());
//
//        Member member = memberRequestDto.toEntity(passwordEncoder);
//        member.setMemberId(maskedId);
//        member.setMemberEmail(maskedEmail);
//        return memberRepository.save(member);
//    }

    // 로그인
    @Override
    public Authentication logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                MemberRequestDto request) throws Exception {
        try {
            Authentication authentication = authenticationConfiguration.getAuthenticationManager()
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getMemberId(),
                                    request.getMemberPw(),
                                    null
                            )
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Member member = ((UserDetailsImpl) authentication.getPrincipal()).getMember();

            // access 토큰 생성
            String accessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
            log.info("access 토큰이 생성되었습니다 : " + accessToken);

            // 쿠키 생성
            tokenProvider.addTokenToCookie(httpRequest, httpResponse, accessToken);

            // refresh 토큰 생성(refresh 토큰 없거나 유효하지 않을 경우)
            String findRefreshToken = redisUtils.getValues(member.getMemberId());

            // refresh 토큰 없는 경우
            if (findRefreshToken == null) {
                RefreshToken refreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
                redisUtils.setValues(member.getMemberId(), refreshToken.getRefreshToken(), TokenProvider.REFRESH_TOKEN_DURATION);
                tokenProvider.addRefreshTokenToCookie(httpRequest, httpResponse, refreshToken.getRefreshToken());
                log.info("refresh 토큰이 생성되었습니다(생성된 토큰) : " + refreshToken.getRefreshToken());
                log.info("refresh 토큰이 생성되었습니다(redis에서 가져온 토큰) : " + redisUtils.getValues(member.getMemberId()));

            // refresh 토큰이 유효하지않은 경우
            } else if (!tokenProvider.validRefreshToken(findRefreshToken)) {
                redisUtils.deleteValues(member.getMemberId());
                RefreshToken newRefreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
                redisUtils.setValues(member.getMemberId(), newRefreshToken.getRefreshToken(), TokenProvider.REFRESH_TOKEN_DURATION);
                tokenProvider.addRefreshTokenToCookie(httpRequest, httpResponse, newRefreshToken.getRefreshToken());
                log.info("refresh 토큰이 생성되었습니다(생성된 토큰) : " + newRefreshToken.getRefreshToken());
                log.info("refresh 토큰이 생성되었습니다(redis에서 가져온 토큰) : " + redisUtils.getValues(member.getMemberId()));
            }

            MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                    .memberNo(member.getMemberNo())
                    .memberId(member.getMemberId())
                    .memberEmail(member.getMemberEmail())
                    .nicknameWithRandomTag(member.getNicknameWithRandomTag())
                    .memberPw(member.getMemberPw())
                    .providerType(member.getProviderType())
                    .role(member.getRole())
                    .createTime(member.getCreateTime())
                    .updateTime(member.getUpdateTime())
                    .accessToken(accessToken)
                    .refreshToken(redisUtils.getValues(member.getMemberId())) // 삭제할 것
                    .build();

            // memberResponseDto 객체를 JSON 문자열로 직렬화
            String jsonResponse = objectMapper.writeValueAsString(memberResponseDto);
            // HTTP 응답 헤더 설정, 클라이언트에게 반환되는 데이터 json 타입
            httpResponse.setContentType("application/json");
            // HTTP 응답 본문에 json 문자열 작성, json 데이터를 클라이언트에게 전송
            httpResponse.getWriter().write(jsonResponse);

            return authentication;

        } catch (AuthenticationException e) {
            log.info("아이디 또는 패스워드가 틀렸습니다");
            throw e;
        }
    }

    // 로그아웃
    @Override
    public void logOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        String authorizationHeader = tokenProvider.getTokenFromCookie(httpRequest);
        String accessToken = tokenProvider.getAccessToken(authorizationHeader);
        String memberId = tokenProvider.getMemberId(accessToken);
        // 사용자 아이디 이용해서 리프레시 토큰 삭제
        redisUtils.deleteValues(memberId);
        redisUtils.setBlackList(memberId, "logout", tokenProvider.getExpiration(accessToken));
        // 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.HEADER_AUTHORIZATION);
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.REFRESH_TOKEN_COOKIE_NAME);
        // 내정보 열람 시 비밀번호 입력을 통해 비밀번호가 일치할 경우 생성되는 쿠키
        CookieUtil.deleteCookie(httpRequest, httpResponse, "isPasswordVerified");
    }

    // 전체 회원 조회
    @Override
    @Transactional(readOnly = true)
    public Page<MyInfoResponseDto> findAll(Pageable pageable) {
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(MyInfoResponseDto::of);
    }

    // 특정 회원 조회
    @Override
    @Transactional(readOnly = true)
    public Member findById(long memberNo) {
        return memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberNo));
    }

    // 회원 수정
    @Override
    @Transactional
    public Member update(long memberNo, MemberRequestDto requestDto) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberNo));
        member.updateNickname(requestDto.getMemberNickname()); // 닉네임만 수정
        return member;
    }

    @Override
    @Transactional
    public Member updateRole(long memberNo, MemberRequestDto requestDto) {
        Member member = memberRepository.findById(memberNo)
            .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberNo));
        member.updateRole(requestDto);
        return member;
    }

    // 회원 삭제(해당 회원의 refresh 토큰도 함께 삭제)
    @Override
    @Transactional
    public void deleteMember(long memberNo, String memberId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        inquiryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 문의사항 모두 삭제
        deliveryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송지 모두 삭제
        deliveryMessageRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송메시지 모두 삭제
        redisUtils.deleteValues(memberId); // 리프레시토큰 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.HEADER_AUTHORIZATION); // 액세스토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.REFRESH_TOKEN_COOKIE_NAME); // 리프레시토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, "isPasswordVerified"); // 회원 정보 확인 시 발급받은 쿠키 삭제
        memberRepository.deleteById(memberNo); // 계정 삭제
    }

    // 회원 삭제(해당 회원의 refresh 토큰도 함께 삭제)
    @Override
    @Transactional
    public void deleteMemberAdmin(Long memberNo, String memberId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        inquiryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 문의사항 모두 삭제
        deliveryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송지 모두 삭제
        deliveryMessageRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송메시지 모두 삭제
        redisUtils.deleteValues(memberId); // 리프레시토큰 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.HEADER_AUTHORIZATION); // 액세스토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.REFRESH_TOKEN_COOKIE_NAME); // 리프레시토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, "isPasswordVerified"); // 회원 정보 확인 시 발급받은 쿠키 삭제
        memberRepository.deleteById(memberNo); // 계정 삭제
    }

    // OAuth2 인증 성공 후 추가 정보 수정 실행(memberNickname)
    @Override
    @Transactional
    public Member updateNickname(long memberNo, MemberNicknameRequestDto memberNicknameRequestDto) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberNo));

        member.updateNickname(memberNicknameRequestDto.getMemberNickname());
        return member;
    }

    // 인증번호 확인(회원가입 시 입력한 인증번호와 redis에 저장된 인증번호 일치하는지 확인)
    @Override
    public boolean verifyCode(String memberEmail, String inputCode) {
        String savedCode = redisUtils.getValues(memberEmail);
        return inputCode.equals(savedCode);
    }

    // 임시비밀번호 발급(비밀번호 찾기에서 해당 이메일로 임시비밀번호 전송)
    @Override
    public void SetTempPassword(String memberEmail, String tempPassword) {
        Optional<Member> optionalMember = memberRepository.findByMemberEmail(memberEmail);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setMemberPw(passwordEncoder.encode(tempPassword));
            memberRepository.save(member);
            log.info("해당 이메일에 임시비밀번호가 전송되었습니다: {}", memberEmail);
        }
    }

    // 아이디 찾기(닉네임, 이메일 이용)
    @Override
    public String findIdByNicknameEmail(String memberNickname, String memberEmail, String inputCode) {
        Member member = memberRepository.findByMemberNicknameAndMemberEmail(memberNickname, memberEmail);
        String savedCode = redisUtils.getValues(memberEmail);
        if (member != null) {
            if (inputCode.equals(savedCode)) {
                return member.getMemberId();
            } else {
                log.info("인증번호가 일치하지않습니다");
            }
        }
        return null;
    }

    // 아이디 찾기 시 닉네임, 이메일에 해당하는 회원이 있는지 검증
    @Override
    public boolean findMemberByNicknameAndEmail(String memberNickname, String memberEmail) {
        Member member = memberRepository.findByMemberNicknameAndMemberEmail(memberNickname, memberEmail);
        if (member != null) {
            log.info("member : {}", member);
            return true;
        }
        log.info("입력정보와 일치하는 회원이 존재하지않습니다");
        return false;
    }

    // 비밀번호 찾기 시 아이디, 이메일에 해당하는 회원이 있는지 검증
    @Override
    public boolean findMemberByIdAndEmail(String memberId, String memberEmail) {
        Member member = memberRepository.findByMemberIdAndMemberEmail(memberId, memberEmail);
        if (member != null) {
            log.info("member : {}", member);
            return true;
        }
        log.info("입력정보와 일치하는 회원이 존재하지않습니다");
        return false;
    }

    // 비밀번호 변경
    @Override
    public boolean changePassword(long memberNo, String changePw, String confirmPw) {
        Optional<Member> optionalMember = memberRepository.findById(memberNo);
        Member member = optionalMember.get();
        if (member != null) {
            log.info("member : {}", member);
            // 변경할 비밀번호와 변경할 비밀번호 재확인 일치하는지 확인
            if (changePw.equals(confirmPw)) {
                member.setMemberPw(passwordEncoder.encode(changePw));
                memberRepository.save(member);
                log.info("비밀번호 변경 성공");
                return true;

            } else {
                log.info("변경할 비밀번호가 일치하지않습니다");
            }
        } else {
            log.info("해당 회원이 존재하지않습니다.");
        }
        return false;
    }

    // 회원가입 시 이메일 중복 확인
    @Override
    public boolean existsByMemberEmail(String memberEmail) {
        return memberRepository.existsByMemberEmail(memberEmail);
    }

    // 회원가입 시 아이디 중복 확인
    @Override
    public boolean existsByMemberId(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }

    // 비밀번호 확인
    @Override
    public boolean checkPassword(HttpServletRequest request, HttpServletResponse response,
                                 String inputPassword, long memberNo) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다"));

        if (passwordEncoder.matches(inputPassword, member.getMemberPw())) {
            setPasswordVerifiedToCookie(request, response, member.getRandomTag());
            return true;
        }
        return false;
    }

    // 쿠키에 비밀번호 확인 상태 저장
    @Override
    public void setPasswordVerifiedToCookie(HttpServletRequest request, HttpServletResponse response, String randomTag) {
        Cookie[] cookies = request.getCookies();
        boolean cookieExists = false;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isPasswordVerified".equals(cookie.getName())) {
                    if (cookie.getValue().equals(randomTag)) {
                        cookieExists = true; // 쿠키가 이미 존재함
                    } else {
                        CookieUtil.deleteCookie(request, response, "isPasswordVerified");
                    }
                    break; // 쿠키를 찾았으면 루프 종료
                }
            }
        }
        // 쿠키가 존재하지 않을 때만 생성
        if (!cookieExists) {
            Cookie cookie = new Cookie("isPasswordVerified", randomTag);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60); // 1시간
            response.addCookie(cookie);
        }
    }

    // 쿠키에서 비밀번호 확인 상태 체크
    @Override
    public boolean isPasswordVerified(HttpServletRequest request, String randomTag) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isPasswordVerified".equals(cookie.getName()) && randomTag.equals(cookie.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    // 권한(ROLE) 조회
    @Override
    @Transactional(readOnly = true)
    public Page<MyInfoResponseDto> getRole(Role role, Pageable pageable) {
        Page<Member> members = memberRepository.findAllByRole(role, pageable);
        return members.map(MyInfoResponseDto::of);
    }

    // 회원 아이디 마스킹 처리
    @Override
    public String idMasking(String memberId) {
        // 2 범위 뒤로는 모두 마스킹 처리
        return memberId.replaceAll("(?<=.{2}).", "*");
    }

    // 회원 이메일 마스킹 처리
    @Override
    public String emailMasking(String memberEmail) {
        // (?<=.{3}) : 앞의 3개 문자(어떤 문자든 상관없음) 뒤에 있는 문자들을 찾고
        // (?<= : 찾으려는 패턴이 어떤 다른 패턴 뒤에 있어야 찾음)
        // (?=[^@]*?@) : @ 앞에 있으면서 @가 나타나지 않는 문자들을 찾고
        // (?= : 찾으려는 패턴이 어떤 다른 패턴 앞에 있어야 찾음)
        return memberEmail.replaceAll("(?<=.{3}).(?=[^@]*?@)", "*");
    }


}
