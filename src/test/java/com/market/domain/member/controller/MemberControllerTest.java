package com.market.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 생성 및 자동 구성
class MemberControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화 위한 클래스(자바 객체를 Json 데이터로 변환 or Json 데이터를 자바 객체로 변환)

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach // 테스트 실행 전 실행하는 메서드
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        memberRepository.deleteAll();
    }

    // 회원 생성 테스트
    @DisplayName("createMember: 회원 생성에 성공한다")
    @Test
    public void createMember() throws Exception {

        // given
        final String url = "/api/members/signup";
        final String memberId = "song12";
        final String memberEmail = "abc12@email.com";
        final String memberPw = "1234";
        final MemberRequestDto memberRequestDto = new MemberRequestDto(memberId, memberEmail, memberPw);

        // 객체 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(memberRequestDto);

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        List<Member> members = memberRepository.findAll();

        assertThat(members.size()).isEqualTo(1); // 크기가 1인지 검증
        assertThat(members.get(0).getMemberId()).isEqualTo(memberId);
        assertThat(members.get(0).getMemberEmail()).isEqualTo(memberEmail);
        assertThat(members.get(0).getMemberPw()).isEqualTo(memberPw);
    }

    // 전체 회원 조회 테스트
    @DisplayName("findAllMembers: 전체 회원 조회에 성공한다")
    @Test
    public void findAllMembers() throws Exception {

        // given
        final String url = "/api/members/list";
        final String memberId = "song1";
        final String memberEmail = "song1@email.com";
        final String memberPw = "1234";

        memberRepository.save(Member.builder()
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberPw(memberPw)
                .build());

        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(memberId))
                .andExpect(jsonPath("$[0].memberEmail").value(memberEmail))
                .andExpect(jsonPath("$[0].memberPw").value(memberPw));
    }

    // 특정 회원 조회 테스트
    @DisplayName("findMember: 특정 회원 조회에 성공한다")
    @Test
    public void findMember() throws Exception {

        // given
        final String url = "/api/members/{memberNo}";
        final String memberId = "song1";
        final String memberEmail = "song1@email.com";
        final String memberPw = "1234";

        Member savedMember = memberRepository.save(Member.builder()
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberPw(memberPw)
                .build());

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, savedMember.getMemberNo()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.memberEmail").value(memberEmail))
                .andExpect(jsonPath("$.memberPw").value(memberPw));
    }

    // 회원 수정 테스트
    @DisplayName("updateMember: 회원 수정에 성공한다")
    @Test
    public void updateMember() throws Exception {

        // given
        final String url = "/api/members/{memberNo}";
        final String memberId = "song2";
        final String memberEmail = "song2@email.com";
        final String memberPw = "1234";

        Member savedMember = memberRepository.save(Member.builder()
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberPw(memberPw)
                .build());

        final String modifiedMemberId = "modifySong2";
        final String modifiedMemberPw = "4321";

        MemberRequestDto memberRequestDto = new MemberRequestDto(modifiedMemberId, modifiedMemberPw);

        // when
        ResultActions resultActions = mockMvc.perform(put(url, savedMember.getMemberNo())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(memberRequestDto)));

        // then
        resultActions.andExpect(status().isOk());

        Member member = memberRepository.findById(savedMember.getMemberNo()).get();

        assertThat(member.getMemberId()).isEqualTo(modifiedMemberId);
        assertThat(member.getMemberPw()).isEqualTo(modifiedMemberPw);
    }

    // 회원 삭제 테스트
    @DisplayName("deleteMember: 회원 삭제에 성공한다")
    @Test
    public void deleteMember() throws Exception {

        // given
        final String url = "/api/members/{id}";
        final String memberId = "song1";
        final String memberEmail = "song1@email.com";
        final String memberPw = "1234";

        Member savedMember = memberRepository.save(Member.builder()
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberPw(memberPw)
                .build());

        // when
        mockMvc.perform(delete(url, savedMember.getMemberNo()))
                .andExpect(status().isOk());

        // then
        List<Member> members = memberRepository.findAll();

        assertThat(members).isEmpty();
    }



}