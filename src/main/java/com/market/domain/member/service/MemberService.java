package com.market.domain.member.service;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.entity.Member;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface MemberService {

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
     */
    void deleteMember(long memberNo);


}
