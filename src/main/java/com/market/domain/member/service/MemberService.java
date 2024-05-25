package com.market.domain.member.service;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import java.util.List;

public interface MemberService {

    /**
     * 로그인
     *
     * @param httpRequest :
     * @param httpResponse :
     * @param request : 입력 요청 정보
     * @return : 회원 Entity
     */
    Authentication logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                         MemberRequestDto request) throws Exception;

    /**
     * 로그아웃
     *
     * @param httpRequest :
     */
    void logOut(HttpServletRequest httpRequest);

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
     * @return : 회원 Entity
     */
    List<Member> findAll();

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
     */
    void deleteMember(long memberNo, String memberId);

}
