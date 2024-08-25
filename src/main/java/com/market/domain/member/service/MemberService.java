package com.market.domain.member.service;

import com.market.domain.member.constant.Role;
import com.market.domain.member.dto.*;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberSearchCond;
import com.market.global.security.oauth2.ProviderType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface MemberService {

    /**
     * 회원 생성
     *
     * @param memberRequestDto : 회원 생성 요청정보
     * @return : 회원 Entity
     */
    MemberResponseDto createMember(MemberRequestDto memberRequestDto, HttpServletRequest request);

//    /**
//     * 로그인
//     *
//     * @param httpRequest  :
//     * @param httpResponse :
//     * @param request      : 입력 요청 정보
//     * @return : 회원 authentication
//     */
//    Authentication logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
//                         MemberRequestDto request) throws Exception;

    /**
      * 로그인
      *
      * @param httpRequest  :
      * @param httpResponse :
      * @param request      : 입력 요청 정보
      * @return             : 회원 authentication
      */
    MemberResponseDto logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                         MemberRequestDto request) throws Exception;

    /**
     * 로그아웃
     *
     * @param httpRequest  :
     * @param httpResponse :
     */
    void logOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 전체 회원 조회
     *
     * @return : 회원 전체 목록
     */
    Page<MemberResponseDto> findAll(Pageable pageable);

    /**
     * memberNo 이용한 특정 회원 조회(관리자만 가능)
     *
     * @param memberNo : 조회할 회원 고유번호
     * @return : 회원 Entity
     */
    Member findById(long memberNo);

    /**
     * memberId 이용한 특정 회원 조회(관리자만 가능)
     *
     * @param memberId : 조회할 회원 아이디 
     * @return         : 회원 Entity
     */
    MemberResponseDto getMemberById(String memberId);

    /**
     * 키워드 검색 회원 목록 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 회원 목록 조회
     */
    Page<MemberResponseDto> searchMembers(MemberSearchCond cond, Pageable pageable);

    /**
     * 회원 수정
     *
     * @param memberNo         : 수정할 회원 고유번호
     * @param memberRequestDto : 회원 수정 요청 정보
     * @return : 회원 Entity
     */
    Member update(long memberNo, MemberRequestDto memberRequestDto);

    /**
     * 닉네임 변경까지 남은 시간 변환해서 반환
     *
     * @param memberNo : 남은 시간 확인할 회원 고유번호
     * @return : 남은 시간
     */
    String getRemainingTime(Long memberNo);

    /**
     * 닉네임 변경 가능 여부 확인(한달에 한번만 변경 가능)
     *
     * @param member : 변경 가능 여부 확인할 member
     * @return : true / false
     */
    boolean canChangeNickname(Member member);

    /**
     * 닉네임 변경까지 남은 시간을 계산하는 메서드
     *
     * @param member : 변경까지 남은 시간 확인할 member
     * @return : 닉네임 변경까지 남은 시간
     */
    Duration timeUntilNextNicknameChange(Member member);

    /**
     * 회원 권한 수정(admin만 가능)
     *
     * @param memberNo         : 수정할 회원 고유번호
     * @param memberRequestDto : 회원 수정 요청 정보
     * @return : 회원 Entity
     */
    Member updateRole(long memberNo, MemberRequestDto memberRequestDto);

    /**
     * 회원 제재 (admin만 가능)
     *
     * @param loginMember : 관리자인지 검증 받을 로그인중인 회원
     * @param memberNo    : 제재할 회원 고유번호
     */
    void warningMember(Member loginMember, Long memberNo);

    /**
     * 회원 제재 해제 (admin만 가능)
     *
     * @param loginMember : 관리자인지 검증 받을 로그인중인 회원
     * @param memberNo    : 제재 해제할 회원 고유번호
     */
    void warningClear(Member loginMember, Long memberNo);
    
    /**
     * 회원 삭제
     *
     * @param memberNo     : 삭제할 회원 고유번호
     * @param memberId     : 삭제할 회원 아이디
     * @param httpRequest  :
     * @param httpResponse :
     */
    void deleteMember(long memberNo, String memberId, HttpServletRequest httpRequest,
        HttpServletResponse httpResponse);

    /**
     * 회원 삭제(admin이 다른 회원 삭제)
     *
     * @param memberNo      : 삭제할 회원 고유번호
     * @param memberId      : 삭제할 회원 아이디
     */
    void deleteMemberAdmin(Long memberNo, String memberId, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * OAuth2 최초 로그인 시 닉네임 수정
     *
     * @param memberNo         : 수정할 회원 고유번호
     * @param memberRequestDto : 회원 수정 요청 정보
     * @return                 : 수정 반영된 회원 정보
     */
    Member updateOAuthAddInfo(long memberNo, MemberRequestDto memberRequestDto);

    /**
     * 회원가입 시 인증번호 일치하는지 확인(검증)
     *
     * @param memberEmail : 해당 회원의 이메일
     * @param inputCode   : 입력한 인증번호
     */
    boolean verifyCode(String memberEmail, String inputCode);

    /**
     * 아이디 찾기
     *
     * @param findIdRequestDto : 이메일, 인증번호 입력
     * @return  : 찾은 아이디
     */
    String findIdByEmail(FindIdRequestDto findIdRequestDto);

    /**
     * 아이디 찾기 시 아래 인자로 해당 회원 있는지 검증
     *
     * @param memberEmail : 해당 회원의 이메일
     */
    boolean findMemberByEmail(String memberEmail);

    /**
     * 임시비밀번호 발급
     *
     * @param memberEmail  : 해당 회원의 이메일
     * @param tempPassword : 임시비밀번호
     */
    void SetTempPassword(String memberEmail, String tempPassword);

    /**
     * 임시비밀번호 발급 시 아래 인자로 해당 회원 있는지 검증
     *
     * @param memberId    : 해당 회원의 아이디
     * @param memberEmail : 해당 회원의 이메일
     */
    boolean findMemberByIdAndEmail(String memberId, String memberEmail);

    /**
     * 비밀번호 변경
     *
     * @param memberNo  : 회원 있는지 확인할 memberNo
     * @param changePw  : 변경할 비밀번호
     * @param confirmPw : 변경할 비밀번호 재확인
     */
    boolean changePassword(long memberNo, String changePw, String confirmPw);

    /**
     * 회원가입 시 탈퇴회원 DB에서 Ip주소 존재하는지 검증
     *
     */
    void validationIpAddr(HttpServletRequest request);

    /**
     * 회원가입 시 회원 DB에서 아이디 중복 확인 및 탈퇴회원에서 아이디 검증
     *
     */
    void validationId(String memberId);

    /**
     * 회원가입 시 회원 DB에서 이메일 중복 확인 및 탈퇴회원에서 이메일 검증
     *
     * @param memberEmail : 해당 이메일 존재하는지 확인할 memberEmail
     */
    void validationEmail(String memberEmail);

    /**
     * 회원가입 시 닉네임에 비속어가 포함되어있는지 검증
     *
     */
    void validationNickname(String memberNickname);

    /**
     * 비밀번호 확인
     *
     * @param inputPassword : 비밀번호 확인 위해 입력한 비밀번호
     * @param memberNo      : 비밀번호 확인할 회원의 고유번호
     * @return : true / false
     */
    boolean checkPassword(HttpServletRequest request, HttpServletResponse response,
        String inputPassword, long memberNo);

    /**
     * OAuth2.0 회원인지 확인
     *
     * @param member : 확인할 회원 객체
     * @return : true / false
     */
    boolean isOAuthMember(Member member);

    /**
     * 비밀번호 확인 시 쿠키에 비밀번호 상태 저장
     *
     * @param response  : 클라이언트에게 응답 보내기 위한 객체
     * @param randomTag : 해당 회원의 randomTag
     */
    void setPasswordVerifiedToCookie(HttpServletRequest request, HttpServletResponse response,
        String randomTag);

    /**
     * 쿠키에서 비밀번호 확인 상태 체크
     *
     * @param request   : 클라이언트의 요청
     * @param randomTag : 해당 회원의 randomTag
     * @return : true / false
     */
    boolean isPasswordVerified(HttpServletRequest request, String randomTag);

    /**
     * 권한 조회
     *
     * @param role : 특정 role
     * @return : role에 일치하는 회원 목록 반환
     */
    Page<MyInfoResponseDto> getRole(Role role, Pageable pageable);

    /**
     * 총 회원 수 구하기
     *
     * @return : 총 회원 수
     */
    Long countMembers();

    /**
     * 권한이 admin인지 확인
     *
     * @return : true / false
     */
    boolean isAdmin();

    /**
     * 가입 경로별 회원 수
     *
     * @return : 가입 경로별 회원 수
     */
    Map<ProviderType, Long> getCountByProviderType();

    /**
     * 다른 회원 신고 기능, 댓글에서 사용 (같은 회원에 대해서 하루에 한번만 가능)
     *
     * @param member : 본인
     * @param requestDto : 신고할 회원 아이디
     */
    void reportMember(Member member, MemberRequestDto requestDto);

    /**
     * 내가 신고한 회원 목록 확인
     *
     * @param memberNo : 확인하려는 회원
     * @return : 신고한 회원 아이디 목록
     */
    String getReportMemberList(Long memberNo);

    /**
     * 나를 신고한 회원 목록 확인
     *
     * @param memberNo : 확인하려는 회원
     * @return : 나를 신고한 회원 아이디 목록
     */
    String getReporterList(Long memberNo);

    /**
     * 회원 아이디 마스킹 처리
     *
     * @param memberId : 마스킹 처리 할 회원 아이디
     */
    String idMasking(String memberId);

    /**
     * 회원 이메일 마스킹 처리
     *
     * @param memberEmail : 마스킹 처리 할 회원 아이디
     */
    String emailMasking(String memberEmail);

    /**
     * 특정 채팅방의 채팅 메시지 수신자 찾기
     *
     * @param roomId 채팅방
     * @param sender 메세지 보낸 사람
     * @return 채팅 메세지의 수신자
     */
    Member findChatRoomRecipient(Long roomId, Member sender);


    Member findByMemberId(String memberId);
}
