package com.market.domain.chatRoom.controller;

import com.market.domain.chat.dto.ChatListResponseDto;
import com.market.domain.chat.service.ChatService;
import com.market.domain.chatRoom.dto.ChatRoomListResponseDto;
import com.market.domain.chatRoom.dto.ChatRoomRequestDto;
import com.market.domain.chatRoom.dto.ChatRoomResponseDto;
import com.market.domain.chatRoom.service.ChatRoomService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    @PostMapping("/chatrooms") // 채팅방 생성
    public ResponseEntity<ApiResponse> createChatRoom(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @ModelAttribute ChatRoomRequestDto requestDto)
        throws IOException {
        chatRoomService.createChatRoom(requestDto, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("채팅방 생성 성공", HttpStatus.CREATED.value()));
    }

    @GetMapping("/chatrooms") // 채팅방 목록 조회 (관리자)
    public ResponseEntity<ChatRoomListResponseDto> getChatRooms() {
        return ResponseEntity.ok(chatRoomService.getChatRooms());
    }

    @GetMapping("/mychatrooms") // 내 채팅방 목록 조회
    public ResponseEntity<ChatRoomListResponseDto> getMyChatRooms(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(chatRoomService.getMyChatRooms(userDetails.getMember()));
    }

    @GetMapping("/chatrooms/{id}")  // 채팅방 단건 조회
    public ResponseEntity<ChatRoomResponseDto> getChatRoom(@PathVariable Long id) {
        return ResponseEntity.ok(chatRoomService.getChatRoom(id));
    }

    @GetMapping("/chatrooms/chat/{id}") // 채팅방 내 채팅 목록 조회
    public ResponseEntity<ChatListResponseDto> getAllChatByRoomId(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getAllChatByRoomId(id));
    }

    @DeleteMapping("/chatrooms/{id}") // 채팅방 삭제
    public ResponseEntity<ApiResponse> deleteChatRoom(@PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.deleteChatRoom(id, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("채팅방 삭제 성공", HttpStatus.OK.value()));

    }
}