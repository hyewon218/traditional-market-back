package com.market.domain.chatRoom.controller;

import com.market.domain.chat.dto.ChatResponseDto;
import com.market.domain.chat.service.ChatService;
import com.market.domain.chatRoom.dto.ChatRoomRequestDto;
import com.market.domain.chatRoom.dto.ChatRoomResponseDto;
import com.market.domain.chatRoom.service.ChatRoomService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    @PostMapping("/chatrooms") // 채팅방 생성
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @ModelAttribute ChatRoomRequestDto requestDto)
        throws IOException {
        ChatRoomResponseDto result = chatRoomService.createChatRoom(requestDto,
            userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/chatrooms/{chatRoomNo}")  // 채팅방 단건 조회
    public ResponseEntity<ChatRoomResponseDto> getChatRoom(@PathVariable Long chatRoomNo) {
        return ResponseEntity.ok().body(chatRoomService.getChatRoom(chatRoomNo));
    }

    @GetMapping("/chatrooms") // 채팅방 목록 조회 (관리자)
    public ResponseEntity<Page<ChatRoomResponseDto>> getChatRooms(Pageable pageable) {
        return ResponseEntity.ok().body(chatRoomService.getChatRooms(pageable));
    }

    @GetMapping("/mychatrooms") // 내 채팅방 목록 조회
    public ResponseEntity<Page<ChatRoomResponseDto>> getMyChatRooms(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        return ResponseEntity.ok()
            .body(chatRoomService.getMyChatRooms(userDetails.getMember(), pageable));
    }

    @GetMapping("/chatrooms/chat/{chatRoomNo}") // 채팅방 내 채팅 목록 조회
    public ResponseEntity<List<ChatResponseDto>> getAllChatByRoomId(@PathVariable Long chatRoomNo) {
        return ResponseEntity.ok().body(chatService.getAllChatByRoomId(chatRoomNo));
    }

    @DeleteMapping("/chatrooms/{chatRoomNo}") // 채팅방 삭제
    public ResponseEntity<ApiResponse> deleteChatRoom(@PathVariable Long chatRoomNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.deleteChatRoom(chatRoomNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("채팅방 삭제 성공", HttpStatus.OK.value()));
    }

    @PutMapping("/chatrooms/{chatRoomNo}/read") // 채팅방 읽음 상태로 변경
    public ResponseEntity<ApiResponse> markChatRoomAsRead(
        @PathVariable Long chatRoomNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.markChatRoomAsRead(chatRoomNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("채팅방 읽음 상태로 변경 성공", HttpStatus.OK.value()));
    }

    @GetMapping("/chatrooms/{chatRoomNo}/read")  // 채팅방 읽음 여부 조회
    public ResponseEntity<ChatRoomResponseDto> getChatRoomIsRead(@PathVariable Long chatRoomNo) {
        return ResponseEntity.ok().body(chatRoomService.getChatRoomIsRead(chatRoomNo));
    }

    @PutMapping("/chatrooms/{chatRoomNo}/unread") // 채팅방 읽지 않은 상태로 변경
    public ResponseEntity<ApiResponse> markChatRoomAsUnRead(
        @PathVariable Long chatRoomNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.markChatRoomAsUnRead(chatRoomNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("채팅방 읽지 않음 상태로 변경 성공", HttpStatus.OK.value()));
    }
}