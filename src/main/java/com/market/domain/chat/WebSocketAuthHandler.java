package com.market.domain.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.chat.dto.ChatMessageDto;
import com.market.domain.chat.service.ChatService;
import com.market.domain.kafka.producer.NotificationProducer;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberService;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.jwt.config.TokenProvider;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// 들어오는 WebSocket 메시지를 관리하기 위한 핸들러
@Log4j2
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final MemberService memberService;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketSessionManager sessionManager = new WebSocketSessionManager();

    // Called when a new WebSocket connection is established
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established, session ID: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Parse the incoming message to determine its type
        Map<String, Object> messageMap = parseMessage(message.getPayload());

        String type = (String) messageMap.get("type");

        if ("AUTH".equals(type)) {
            handleAuthMessage(session, messageMap);
        } else if ("CHAT".equals(type)) {
            handleChatMessage(session, messageMap);
        } else if ("HEARTBEAT".equals(type)) {
            log.info("Heartbeat received from session ID: {}", session.getId());
        } else {
            log.warn("Unknown message type received: {}", type);
        }
    }

    private void handleAuthMessage(WebSocketSession session, Map<String, Object> messageMap) throws IOException {
        String token = (String) messageMap.get("token");
        Long roomId = ((Number) messageMap.get("roomId")).longValue();

        // Validate the JWT token
        if (token == null || !tokenProvider.validToken(tokenProvider.getAccessToken(token))) {
            log.error("Invalid JWT token: {}", token);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid JWT token"));
            throw new BusinessException(ErrorCode.INVALID_AUTH_TOKEN);
        }

        // Store the session in the session manager linked to the room
        sessionManager.addSessionToRoom(roomId, session);
        session.getAttributes().put("roomId", roomId); // Store roomId in session attributes
        log.info("Session ID: {} authorized for room ID: {}", session.getId(), roomId);
    }

    private void handleChatMessage(WebSocketSession session, Map<String, Object> messageMap) throws IOException {
        try {
            ChatMessageDto messageDto = objectMapper.convertValue(messageMap, ChatMessageDto.class);
            Long roomId = messageDto.getRoomId();

            // Save the chat message to the database
            chatService.saveWebSocketMessage(roomId, messageDto);

            Member sender = memberService.findByMemberId(messageDto.getSender());
            List<Member> receivers = memberService.findChatRoomRecipients(roomId, sender);

            // Send notifications to other chat room members
            if (receivers != null && !receivers.isEmpty()) {
                NotificationArgs notificationArgs = NotificationArgs.of(sender.getMemberNo(), roomId);
                for (Member receiver : receivers) {
                    NotificationType notificationType = getNotificationType(receiver);
                    notificationService.send(notificationType, notificationArgs, receiver.getMemberNo());
                    /*notificationProducer.send(new NotificationEvent(notificationType, notificationArgs,
                        receiver.getMemberNo()));*/
                }
            }

            // Broadcast the chat message to all WebSocket clients in the room
            broadcastMessageToRoom(roomId, messageDto);
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage());
            // Handle or notify user of error
        }
    }

    private Map<String, Object> parseMessage(String payload) throws JsonProcessingException {
        // 수신 JSON 페이로드를 맵으로 Parse
        return objectMapper.readValue(payload, Map.class);
    }

    private void broadcastMessageToRoom(Long roomId, ChatMessageDto messageDto) throws IOException {
        // Get the list of WebSocket sessions for the room
        List<WebSocketSession> roomSessions = sessionManager.getSessionsForRoom(roomId);
        for (WebSocketSession session : roomSessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageDto)));
            }
        }
    }

    private static NotificationType getNotificationType(Member receiver) {
        NotificationType notificationType;
        if (receiver.getRole() == Role.ADMIN) {
            notificationType = NotificationType.NEW_CHAT_REQUEST_ON_CHATROOM;
        } else if (receiver.getRole() == Role.MEMBER) {
            notificationType = NotificationType.NEW_CHAT_ON_CHATROOM;
        } else {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }
        return notificationType;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Error in WebSocket transport for session ID: {}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
        // Remove session from the associated room
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed, session ID: {}, close status: {}", session.getId(), status);
        // Remove the session from the appropriate room
        removeSession(session);
    }

    private void removeSession(WebSocketSession session) {
        // Implement logic to determine the roomId from the session (if stored)
        Long roomId = getRoomIdFromSession(session); // You need to implement this method based on your application logic
        if (roomId != null) {
            sessionManager.removeSessionFromRoom(roomId, session);
            log.info("Removed session ID: {} from room ID: {}", session.getId(), roomId);
        }
    }

    private Long getRoomIdFromSession(WebSocketSession session) {
        // Retrieve the roomId from session attributes
        return (Long) session.getAttributes().get("roomId");
    }
}