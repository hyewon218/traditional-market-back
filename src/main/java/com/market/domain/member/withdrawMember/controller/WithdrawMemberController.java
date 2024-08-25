package com.market.domain.member.withdrawMember.controller;

import com.market.domain.member.withdrawMember.dto.WithdrawMemberResponseDto;
import com.market.domain.member.withdrawMember.repository.WithdrawMemberSearchCond;
import com.market.domain.member.withdrawMember.service.WithdrawMemberService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdrawmembers")
public class WithdrawMemberController {

    private final WithdrawMemberService withdrawMemberService;

    // 전체 조회
    @GetMapping("")
    public ResponseEntity<Page<WithdrawMemberResponseDto>> getAllWithdrawMembers(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        Page<WithdrawMemberResponseDto> withdrawMembers = withdrawMemberService.getAllWithdrawMembers(
            userDetails.getMember(), pageable);
        return ResponseEntity.ok().body(withdrawMembers);
    }

    // 키워드 검색 탈퇴회원 목록 조회
    @GetMapping("/search")
    public ResponseEntity<Page<WithdrawMemberResponseDto>> searchWithdrawMembers(
        WithdrawMemberSearchCond cond,
        Pageable pageable) {
        Page<WithdrawMemberResponseDto> result = withdrawMemberService.searchWithdrawMembers(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    // 단건 조회
    @GetMapping("/{withdrawMemberNo}")
    public ResponseEntity<WithdrawMemberResponseDto> getWithdrawMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long withdrawMemberNo) {
        WithdrawMemberResponseDto withdrawMember = withdrawMemberService.getWithdrawMember(
            userDetails.getMember(), withdrawMemberNo);
        return ResponseEntity.ok().body(withdrawMember);
    }

    // 삭제
    @DeleteMapping("/{withdrawMemberNo}")
    public ResponseEntity<ApiResponse> deleteWithdrawMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long withdrawMemberNo) {
        withdrawMemberService.deleteWithdrawMember(userDetails.getMember(), withdrawMemberNo);
        return ResponseEntity.ok().body(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // 전체 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse> deleteAll(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        withdrawMemberService.deleteAll(userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("전체 삭제 성공", HttpStatus.OK.value()));
    }

}
