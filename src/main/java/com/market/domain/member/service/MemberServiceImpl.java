package com.market.domain.member.service;

import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.chatRoom.repository.ChatRoomRepository;
import com.market.domain.delivery.repository.DeliveryRepository;
import com.market.domain.deliveryMessage.repository.DeliveryMessageRepository;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.FindIdRequestDto;
import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.dto.MyInfoResponseDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.repository.MemberRepositoryQuery;
import com.market.domain.member.repository.MemberSearchCond;
import com.market.domain.member.withdrawMember.service.WithdrawMemberService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.profanityFilter.ProfanityFilter;
import com.market.global.redis.RedisUtils;
import com.market.global.security.CookieUtil;
import com.market.global.security.UserDetailsImpl;
import com.market.global.security.oauth2.ProviderType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenProvider tokenProvider;
    private final RedisUtils redisUtils;
    private final DeliveryRepository deliveryRepository;
    private final InquiryRepository inquiryRepository;
    private final DeliveryMessageRepository deliveryMessageRepository;
    private final MemberRepositoryQuery memberRepositoryQuery;
    private final WithdrawMemberService withdrawMemberService;
    private final ChatRoomRepository chatRoomRepository;

    // 회원 생성
    @Override
    @Transactional
    public MemberResponseDto createMember(MemberRequestDto memberRequestDto,
        HttpServletRequest request) {

        validationId(memberRequestDto.getMemberId()); // 가입하려는 id가 회원 DB, 탈퇴회원 DB에 있는지 검증
        validationEmail(memberRequestDto.getMemberEmail()); // 가입하려는 email 이 회원 DB, 탈퇴회원 DB에 있는지 검증
        validationNickname(memberRequestDto.getMemberNickname()); // 닉네임에 비속어가 포함되어있는지 검증
        return MemberResponseDto.of(
            memberRepository.save(memberRequestDto.toEntity(passwordEncoder)));
    }

    // 로그인
    @Override
    public MemberResponseDto logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        MemberRequestDto request) throws Exception {

        if (withdrawMemberService.existsMemberId(request.getMemberId())) { // 탈퇴회원 DB에 존재 여부 검증
            throw new BusinessException(ErrorCode.EXISTS_WITHDRAWMEMBER_ID);
        }

        try {
            Authentication authentication = authenticationConfiguration.getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(
                    request.getMemberId(),
                    request.getMemberPw(),
                    null
                ));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Member member = ((UserDetailsImpl) authentication.getPrincipal()).getMember();

            String accessToken = tokenProvider.generateToken(member,
                TokenProvider.ACCESS_TOKEN_DURATION);
            tokenProvider.addTokenToCookie(httpRequest, httpResponse, accessToken);

            String findRefreshToken = redisUtils.getValues(member.getMemberId());
            String newRefreshToken = null;

            // refresh 토큰 없는 경우
            if (findRefreshToken == null) {
                RefreshToken refreshToken = tokenProvider.generateRefreshToken(member,
                    TokenProvider.REFRESH_TOKEN_DURATION);
                redisUtils.setValues(member.getMemberId(), refreshToken.getRefreshToken(),
                    TokenProvider.REFRESH_TOKEN_DURATION);
                tokenProvider.addRefreshTokenToCookie(httpRequest, httpResponse,
                    refreshToken.getRefreshToken());
                newRefreshToken = refreshToken.getRefreshToken();

                // refresh 토큰이 유효하지 않은 경우
            } else if (!tokenProvider.validRefreshToken(findRefreshToken, httpRequest,
                httpResponse)) {
                redisUtils.deleteValues(member.getMemberId());
                RefreshToken refreshToken = tokenProvider.generateRefreshToken(member,
                    TokenProvider.REFRESH_TOKEN_DURATION);
                redisUtils.setValues(member.getMemberId(), refreshToken.getRefreshToken(),
                    TokenProvider.REFRESH_TOKEN_DURATION);
                tokenProvider.addRefreshTokenToCookie(httpRequest, httpResponse,
                    refreshToken.getRefreshToken());
                newRefreshToken = refreshToken.getRefreshToken();
            }
            return MemberResponseDto.ofLogin(member, accessToken, newRefreshToken);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_CORRECT_ID_PW);
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

    // 전체 회원 조회(admin 만 가능)
    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponseDto> findAll(Pageable pageable) {
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(MemberResponseDto::of);
    }

    // 키워드 검색 회원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponseDto> searchMembers(MemberSearchCond cond, Pageable pageable) {
        return memberRepositoryQuery.searchMembers(cond, pageable).map(MemberResponseDto::of);
    }

    // 회원 수정 (닉네임 변경)
    @Override
    @Transactional
    public Member update(long memberNo, MemberRequestDto requestDto) {
        Member member = findById(memberNo);

        // 변경하려는 닉네임에 비속어 있는지 검증
        validationNickname(requestDto.getMemberNickname());

        // 닉네임 변경 가능 여부 확인
        if (canChangeNickname(member)) {
            member.updateNickname(requestDto.getMemberNickname()); // 닉네임만 수정
            return member;

        } else {
            // 닉네임 변경까지 남은 시간 계산
            Duration duration = timeUntilNextNicknameChange(member);
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() % 60;
            log.info("닉네임 변경까지 남은 시간: " + minutes + "분 " + seconds + "초");
            throw new RuntimeException("닉네임 변경은 " + minutes + "분 " + seconds + "초 후에 가능합니다.");
        }
    }

    // 닉네임 변경 가능까지 남은 시간 변환해 알려주는 메서드(timeUntilNextNicknameChange, formatDuration 메서드 이용)
    @Override
    @Transactional(readOnly = true)
    public String getRemainingTime(Long memberNo) {
        Member findMember = findById(memberNo);
        Duration duration = timeUntilNextNicknameChange(findMember);

        if (duration.isZero()) {
            return "닉네임 변경이 가능합니다.";
        } else {
            // 닉네임 변경까지 남은 시간 반환
            return "닉네임 변경까지 " + formatDuration(duration) + " 남았습니다.";
        }
    }

    // 닉네임 변경 가능한지 계산(한달 지났으면 변경 가능, 안 지났으면 변경 불가)
    @Override
    public boolean canChangeNickname(Member member) {
        if (member.getLastNicknameChangeDate() == null) {
            return true;
        }
        return member.getLastNicknameChangeDate().plusMonths(1).isBefore(LocalDateTime.now());
    }

    // 닉네임 변경까지 남은 시간을 계산
    @Override
    public Duration timeUntilNextNicknameChange(Member member) {
        if (member.getLastNicknameChangeDate() == null) {
            return Duration.ZERO; // 닉네임을 아직 변경하지 않은 경우
        }
        LocalDateTime nextAllowedChangeDate = member.getLastNicknameChangeDate().plusMonths(1);
        return Duration.between(LocalDateTime.now(), nextAllowedChangeDate).isNegative()
            ? Duration.ZERO // 현재 시간이 다음 가능한 변경 시간 이후일 때
            : Duration.between(LocalDateTime.now(), nextAllowedChangeDate);
    }

    // 닉네임 변경 남은 시간 변환
    public String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("일 ");
        }
        if (hours > 0) {
            sb.append(hours).append("시간 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("분 ");
        }
        if (remainingSeconds > 0) {
            sb.append(remainingSeconds).append("초 ");
        }

        return sb.toString().trim();
    }

    // 권한 변경
    @Override
    @Transactional
    public void updateRole(Member member, long memberNo, MemberRequestDto requestDto) {
        validationAdmin(member);
        Member findMember = findById(memberNo);
        findMember.updateRole(requestDto);
    }

    // 특정 회원 제재 (댓글, 일대일 채팅 상담 30일간 제한)
    @Override
    @Transactional
    public void warningMember(Member loginMember, Long memberNo) {
        validationAdmin(loginMember);
        Member findMember = findById(memberNo);
        findMember.setIsWarning(true);
        findMember.setWarningTime(LocalDateTime.now());
    }

    // 특정 회원 제재 해제 (직접 해제할 때는 countWarning(제재 누적 횟수 증가하지 않음))
    @Override
    @Transactional
    public void warningClear(Member loginMember, Long memberNo) {
        validationAdmin(loginMember);
        Member findMember = findById(memberNo);
        findMember.setIsWarning(false);
        findMember.setWarningTime(null);
    }

    // 매일 0시 30분에 실행, 제재일 30일 지났는지 확인하고 30일 지났으면 isWarning 값 false 로 자동 변경
    @Scheduled(cron = "0 30 0 * * ?") // 스케줄러 동시 실행될 경우 자원 경합 문제로 시간 나눠서 설정
    @Transactional
    public void updateIsWarning() {
        log.info("제재 여부 업데이트 스케줄러 실행");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        // isWarning(제재 여부)이 true 인 사용자 중 warningStartDate 가 30일 이전인 사용자 찾기
        memberRepository.findByIsWarningAndWarningStartDateBefore(true, thirtyDaysAgo)
            .forEach(member -> {
                member.setIsWarning(false);
                member.setCountWarning(member.getCountWarning()); // 제재 누적 횟수 증가
                member.setWarningStartDate(null); // 제재 시작 시간 초기화
                memberRepository.save(member);
            });
    }

    // 회원 삭제(해당 회원의 refresh 토큰도 함께 삭제)
    @Override
    @Transactional
    public void deleteMember(long memberNo, String memberId, HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {

        withdrawMemberService.createWithdrawMember(findById(memberNo)); // 탈퇴회원에 정보 추가
        inquiryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 문의사항 모두 삭제
        deliveryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송지 모두 삭제
        deliveryMessageRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송메시지 모두 삭제
        redisUtils.deleteValues(memberId); // 리프레시토큰 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse,
            TokenProvider.HEADER_AUTHORIZATION); // 액세스토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse,
            TokenProvider.REFRESH_TOKEN_COOKIE_NAME); // 리프레시토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse,
            "isPasswordVerified"); // 회원 정보 확인 시 발급받은 쿠키 삭제
        memberRepository.deleteById(memberNo); // 계정 삭제
    }

    // 관리자가 특정 회원 삭제(해당 회원의 refresh 토큰도 함께 삭제)
    @Override
    @Transactional
    public void deleteMemberAdmin(Member member, Long memberNo, String memberId,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {

        validationAdmin(member);
        withdrawMemberService.createWithdrawMember(findById(memberNo)); // 탈퇴회원에 정보 추가
        inquiryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 문의사항 모두 삭제
        deliveryRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송지 모두 삭제
        deliveryMessageRepository.deleteAllByMemberNo(memberNo); // 해당 회원의 배송메시지 모두 삭제
        redisUtils.deleteValues(memberId); // 리프레시토큰 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse,
            TokenProvider.HEADER_AUTHORIZATION); // 액세스토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse,
            TokenProvider.REFRESH_TOKEN_COOKIE_NAME); // 리프레시토큰 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse,
            "isPasswordVerified"); // 회원 정보 확인 시 발급받은 쿠키 삭제
        memberRepository.deleteById(memberNo); // 계정 삭제
    }

    // OAuth2 인증 성공 후 추가 정보 수정 실행(memberNickname)
    @Override
    @Transactional
    public Member updateOAuthAddInfo(long memberNo, MemberRequestDto memberRequestDto) {
        Member member = findById(memberNo);

        // 전송하려는 채팅 메세지에 비속어 포함되어있는지 검증
        validationNickname(memberRequestDto.getMemberNickname());
        member.updateOAuthInfo(memberRequestDto.getMemberNickname());
        return memberRepository.save(member);
    }

    // 인증번호 확인(회원가입 시 입력한 인증번호와 redis 에 저장된 인증번호 일치하는지 확인)
    @Override
    @Transactional(readOnly = true)
    public void verifyCode(String memberEmail, String inputCode) {
        String savedCode = redisUtils.getValues(memberEmail);
        if (!inputCode.equals(savedCode)) {
            throw new BusinessException(ErrorCode.NOT_CORRECT_CODE);
        }
    }

    // 아이디 찾기(이메일 이용, 추후 member Entity 에 휴대전화번호필드 추가해서 휴대전화번호도 같이 이용하는 걸로 변경하기)
    @Override
    @Transactional(readOnly = true)
    public String findIdByEmail(FindIdRequestDto findIdRequestDto) {
        Member member = findByMemberEmail(findIdRequestDto.getMemberEmail());
        String savedCode = redisUtils.getValues(member.getMemberEmail());

        if (findIdRequestDto.getCode().equals(savedCode)) {
            return member.getMemberId();
        }
        return null;
    }

    // 아이디 찾기 시 닉네임, 이메일에 해당하는 회원이 있는지 검증
    @Override
    @Transactional(readOnly = true)
    public boolean findMemberByEmail(String memberEmail) {
        Member member = findByMemberEmail(memberEmail);
        return member != null;
    }

    // 임시비밀번호 발급(비밀번호 찾기에서 해당 이메일로 임시비밀번호 전송)
    @Override
    @Transactional
    public void SetTempPassword(String memberEmail, String tempPassword) {
        Member findMember = findByMemberEmail(memberEmail);
        findMember.setMemberPw(passwordEncoder.encode(tempPassword));
        memberRepository.save(findMember);
    }

    // 임시비밀번호 발급 시 아이디, 이메일에 해당하는 회원이 있는지 검증
    @Override
    @Transactional(readOnly = true)
    public boolean findMemberByIdAndEmail(String memberId, String memberEmail) {
        Member member = memberRepository.findByMemberIdAndMemberEmail(memberId, memberEmail);
        return member != null;
    }

    // 비밀번호 변경
    @Override
    @Transactional
    public void changePassword(long memberNo, String changePw, String confirmPw) {
        Member findMember = findById(memberNo);
        // 변경할 비밀번호와 변경할 비밀번호 재확인 일치하는지 확인
        if (changePw.equals(confirmPw)) {
            findMember.updatePw(passwordEncoder.encode(changePw));
            memberRepository.save(findMember);
        } else {
            throw new BusinessException(ErrorCode.FAIL_TO_CHANGE_PW);
        }
    }

    // 회원가입 시 회원 DB 에서 아이디 중복 확인 및 탈퇴회원에서 아이디 검증
    @Override
    public void validationId(String memberId) {
        if (ProfanityFilter.containsProfanity(memberId)) { // 비속어 검증
            throw new BusinessException(ErrorCode.NOT_ALLOW_PROFANITY_ID);
        } else if (memberRepository.existsByMemberId(memberId)) { // 회원 DB에 존재 여부 검증
            throw new BusinessException(ErrorCode.EXISTS_ID);
        } else if (withdrawMemberService.existsMemberId(memberId)) { // 탈퇴회원 DB에 존재 여부 검증
            throw new BusinessException(ErrorCode.EXISTS_WITHDRAWMEMBER_ID);
        }
    }

    // 회원가입 시 회원 DB 에서 이메일 중복 확인 및 탈퇴회원에서 이메일 검증
    @Override
    public void validationEmail(String memberEmail) {
        if (memberRepository.existsByMemberEmail(memberEmail)) {
            throw new BusinessException(ErrorCode.EXISTS_EMAIL);
        } else if (withdrawMemberService.existsMemberEmail(memberEmail)) {
            throw new BusinessException(ErrorCode.EXISTS_WITHDRAWMEMBER_EMAIL);
        }
    }

    // 회원가입 시 닉네임에 비속어가 포함되어있는지 검증
    @Override
    public void validationNickname(String memberNickname) {
        if (ProfanityFilter.containsProfanity(memberNickname)) {
            throw new BusinessException(ErrorCode.NOT_ALLOW_PROFANITY_NICKNAME);
        }
    }

    // 비밀번호 확인
    @Override
    @Transactional(readOnly = true)
    public void checkPassword(HttpServletRequest request, HttpServletResponse response,
        String inputPassword, long memberNo) {
        Member member = findById(memberNo);

        if (passwordEncoder.matches(inputPassword, member.getMemberPw()) || isOAuthMember(member)) {
            setPasswordVerifiedToCookie(request, response, member.getRandomTag());
        } else {
            throw new BusinessException(ErrorCode.NOT_CORRECT_PW);
        }
    }

    // OAuth2 로그인 회원의 경우, 별도의 인증 프로세스(이미 인증된 상태)
    @Override
    @Transactional(readOnly = true)
    public boolean isOAuthMember(Member member) {
        Member oAuthMember = findById(member.getMemberNo());
        return oAuthMember.getProviderType() != ProviderType.LOCAL;
    }

    // 쿠키에 비밀번호 확인 상태 저장
/*    @Override
    public void setPasswordVerifiedToCookie(HttpServletRequest request,
        HttpServletResponse response, String randomTag) {
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
    }*/

    // 쿠키에 비밀번호 확인 상태 저장
    @Override
    public void setPasswordVerifiedToCookie(HttpServletRequest request,
        HttpServletResponse response, String randomTag) {
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
            ResponseCookie cookie = ResponseCookie.from("isPasswordVerified", randomTag)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .httpOnly(true)
                //.secure(true) // https 에서만 작동
                .build();
            response.addHeader("Set-Cookie", cookie.toString());
        }
    }

    // 쿠키에서 비밀번호 확인 상태 체크
    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordVerified(HttpServletRequest request, String randomTag) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isPasswordVerified".equals(cookie.getName()) && randomTag.equals(
                    cookie.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    // 권한(ROLE) 별 조회
    @Override
    @Transactional(readOnly = true)
    public Page<MyInfoResponseDto> getRole(Role role, Pageable pageable) {
        Page<Member> members = memberRepository.findAllByRole(role, pageable);
        return members.map(MyInfoResponseDto::of);
    }

    // 총 회원 수
    @Override
    @Transactional(readOnly = true)
    public Long countMembers() {
        return memberRepository.count();
    }

    // admin 권한 검증
    public void validationAdmin(Member member) {
        if (member.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    // providerType별 회원 수
    @Override
    public Map<ProviderType, Long> getCountByProviderType() {
        Map<ProviderType, Long> countMap = new HashMap<>();

        for (ProviderType type : ProviderType.values()) {
            long count = memberRepository.countByProviderType(type);
            countMap.put(type, count);
        }
        return countMap;
    }

    // 다른 회원 신고 기능, 댓글에서 사용 (같은 회원에 대해서 하루에 한번만 가능)
    @Override
    @Transactional
    public void reportMember(Member member, MemberRequestDto requestDto) {
        if (member.getMemberId().equals(requestDto.getMemberId())) {
            throw new IllegalArgumentException("본인은 신고할 수 없습니다.");
        }

        List<String> todayReportMember = member.getReportMember();
        if (todayReportMember.contains(requestDto.getMemberId())) {
            throw new IllegalArgumentException("이미 신고한 회원입니다.");
        }

        Member reportMember = findByMemberId(requestDto.getMemberId());

//            if (reportMember.getRole() == Role.ADMIN) {
//                throw new IllegalArgumentException("관리자는 신고할 수 없습니다.");
//            }

        reportMember.setCountReport(); // 신고하려는 회원의 신고 누적 횟수 +1 증가
        reportMember.getReporters().add(member.getMemberId()); // 나를 신고한 회원 목록에 본인 아이디 추가
        memberRepository.save(reportMember); // 변경 사항 DB에 저장

        todayReportMember.add(requestDto.getMemberId()); // 오늘 신고한 회원 목록에 신고하려는 회원 아이디 추가
        memberRepository.save(member); // 변경 사항 DB에 저장
    }

    // 모든 회원의 reportMember 리스트를 빈 리스트로 설정 (내가 신고한 회원 목록 초기화)
    @Scheduled(cron = "0 0 1 * * ?") // 매일 오전 1시 실행
    @Transactional
    public void clearReportMemberList() {
        log.info("clearReportMemberList 스케줄러 실행");
        memberRepository.clearAllReportMemberLists(new ArrayList<>());
    }

    // 내가 신고한 사람 목록 확인
    @Override
    @Transactional(readOnly = true)
    public String getReportMemberList(Long memberNo) {
        Member member = findById(memberNo);
        List<String> todayReportMember = member.getReportMember();
        return String.join(", ", todayReportMember);
    }

    // 나를 신고한 사람 목록 확인
    @Override
    @Transactional(readOnly = true)
    public String getReporterList(Long memberNo) {
        Member member = findById(memberNo);
        List<String> reporters = member.getReporters();
        return String.join(", ", reporters);
    }

    // memberNo 이용한 특정 회원 조회(관리자만 가능)
    @Override
    @Transactional(readOnly = true)
    public Member findById(long memberNo) {
        return memberRepository.findById(memberNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEMBER));
    }

    // memberId 이용한 특정 회원 조회(관리자만 가능)
    @Override
    @Transactional(readOnly = true)
    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEMBER));
    }

    // memberEmail 이용한 특정 회원 조회
    @Override
    @Transactional(readOnly = true)
    public Member findByMemberEmail(String memberEmail) {
        return memberRepository.findByMemberEmail(memberEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEMBER));
    }

    // 회원 아이디 마스킹 처리
    @Override
    @Transactional(readOnly = true)
    public String idMasking(String memberId) {
        // 2 범위 뒤로는 모두 마스킹 처리
        return memberId.replaceAll("(?<=.{4}).", "*");
    }

    // 회원 이메일 마스킹 처리
    @Override
    @Transactional(readOnly = true)
    public String emailMasking(String memberEmail) {
        // (?<=.{3}) : 앞의 3개 문자(어떤 문자든 상관없음) 뒤에 있는 문자들을 찾고
        // (?<= : 찾으려는 패턴이 어떤 다른 패턴 뒤에 있어야 찾음)
        // (?=[^@]*?@) : @ 앞에 있으면서 @가 나타나지 않는 문자들을 찾고
        // (?= : 찾으려는 패턴이 어떤 다른 패턴 앞에 있어야 찾음)
        return memberEmail.replaceAll("(?<=.{4}).(?=[^@]*?@)", "*");
    }

    @Override
    public List<Member> findChatRoomRecipients(Long roomId, Member sender) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        return chatRoom.getRecipients(sender);  // 수신자 목록을 반환
    }
}