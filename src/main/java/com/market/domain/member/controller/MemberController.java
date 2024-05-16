package com.market.domain.member.controller;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberServiceImpl memberService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Member> createMember(@RequestBody MemberRequestDto memberRequestDto) {

        Member savedMember = memberService.createMember(memberRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMember);
    }

    // 전체 회원 조회
    @GetMapping("/list")
    public ResponseEntity<List<MemberResponseDto>> findAllMember() {

        List<MemberResponseDto> members = memberService.findAll()
                .stream()
                .map(MemberResponseDto::new)
                .toList();

        return ResponseEntity.ok()
                .body(members);
    }
    
    // 특정 회원 조회
    @GetMapping("/{memberNo}")
    public ResponseEntity<MemberResponseDto> findMember(@PathVariable long memberNo) {

        Member member = memberService.findById(memberNo);
        
        return ResponseEntity.ok()
                .body(new MemberResponseDto(member));
    }

    // 회원 수정
    @PutMapping("/{memberNo}")
    public ResponseEntity<Member> updateMember(@PathVariable long memberNo, @RequestBody MemberRequestDto memberRequestDto) {

        Member updatedMember = memberService.update(memberNo, memberRequestDto);

        return ResponseEntity.ok()
                .body(updatedMember);
    }


    // 회원 삭제
    @DeleteMapping("/{memberNo}")
    public ResponseEntity<Void> deleteMember(@PathVariable long memberNo) {

        memberService.deleteMember(memberNo);

        return ResponseEntity.ok()
                .build();
    }


}
