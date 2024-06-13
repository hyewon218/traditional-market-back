package com.market.domain.member.service;

import com.market.domain.member.dto.MemberNicknameRequestDto;
import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MemberService {

    /**
     * 로그인
     *
     * @param httpRequest  :
     * @param httpResponse :
     * @param request      : 입력 요청 정보
     * @return : 회원 authentication
     */
    Authentication logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                         MemberRequestDto request) throws Exception;

    /**
     * 로그아웃
     *
     * @param httpRequest :
     * @param httpResponse :
     */
    void logOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 회원 생성
     *
     * @param memberRequestDto : 회원 생성 요청정보
     * @return : 회원 Entity
     */
    Member createMember(MemberRequestDto memberRequestDto);

    /**
     * 전체 회원 조회
     *
     * @return : 회원 전체 목록
     */
//    List<Member> findAll();
    List<MemberResponseDto> findAll();

    /**
     * 특정 회원 조회
     *
     * @param memberNo : 조회할 회원 고유번호
     * @return : 회원 Entity
     */
    Member findById(long memberNo);

    /**
     * 회원 수정
     *
     * @param memberNo : 수정할 회원 고유번호
     * @param memberRequestDto : 회원 수정 요청 정보
     * @return : 회원 Entity
     */
    Member update(long memberNo, MemberRequestDto memberRequestDto);
    
    /**
     * 회원 삭제
     *
     * @param memberNo : 삭제할 회원 고유번호
     * @param memberId : 삭제할 회원 아이디
     * @param httpRequest :
     * @param httpResponse :
     */
    void deleteMember(long memberNo, String memberId, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * OAuth2 최초 로그인 시 닉네임 수정
     *
     * @param memberNo : 수정할 회원 고유번호
     * @param memberNicknameRequestDto : 회원 수정 요청 정보
     */
    Member updateNickname(long memberNo, MemberNicknameRequestDto memberNicknameRequestDto);

    /**
     * 회원가입 시 인증번호 일치하는지 확인(검증)
     *
     * @param memberEmail : 해당 회원의 이메일
     * @param inputCode : 입력한 인증번호
     */
    boolean verifyCode(String memberEmail, String inputCode);

    /**
     * 임시비밀번호 발급
     *
     * @param memberEmail : 해당 회원의 이메일
     * @param tempPassword : 임시비밀번호
     */
    void SetTempPassword(String memberEmail, String tempPassword);

    /**
     * 아이디 찾기
     *
     * @param memberNickname : 해당 회원의 닉네임
     * @param memberEmail : 해당 회원의 이메일
     * @param inputCode : 입력한 인증번호
     */
    String findIdByNicknameEmail(String memberNickname, String memberEmail, String inputCode);

    /**
     * 아이디 찾기 시 아래 인자로 해당 회원 있는지 검증
     *
     * @param memberNickname : 해당 회원의 닉네임
     * @param memberEmail : 해당 회원의 이메일
     */
    boolean findMemberByNicknameAndEmail(String memberNickname, String memberEmail);

    /**
     * 비밀번호 찾기 시 아래 인자로 해당 회원 있는지 검증
     *
     * @param memberId : 해당 회원의 아이디
     * @param memberEmail : 해당 회원의 이메일
     */
    boolean findMemberByIdAndEmail(String memberId, String memberEmail);

    /**
     * 비밀번호 변경
     *
     * @param memberNo : 회원 있는지 확인할 memberNo
     * @param currentPw : 현재 비밀번호
     * @param changePw : 변경할 비밀번호
     * @param confirmPw : 변경할 비밀번호 재확인
     */
    boolean changePassword(long memberNo, String currentPw, String changePw, String confirmPw);

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

}
