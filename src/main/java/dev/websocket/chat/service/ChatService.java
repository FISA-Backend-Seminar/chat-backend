package dev.websocket.chat.service;

import dev.websocket.base.Util;
import dev.websocket.chat.dto.ChatMessage;
import dev.websocket.chat.dto.ChatRoom;
import dev.websocket.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    /** 사용자가 name만 주면 서버가 roomId(UUID)를 발급 */
    public ChatRoom createRoom(String name) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom room = ChatRoom.of(roomId, name);
        chatRepository.save(roomId, room);
        log.info("Created room: {} ({})", roomId, name);
        return room;
    }

    public List<ChatRoom> findAll() {
        return chatRepository.findAll();
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRepository.findById(roomId);
    }

    /** 메시지 액션 처리 (ENTER / TALK) — 자동 생성 금지 */
    public void handleAction(String roomId, WebSocketSession session, ChatMessage chatMessage) {
        try {
            if (roomId == null || roomId.isBlank()) {
                sendError(session, "ROOM_ID_REQUIRED");
                return;
            }

            ChatRoom room = chatRepository.findById(roomId);
            if (chatMessage.getMessageType() == ChatMessage.MessageType.ENTER) {
                if (room == null) { // ❌ 없는 방이면 에러
                    sendError(session, "ROOM_NOT_FOUND");
                    return;
                }
                room.join(session);
                session.getAttributes().put("roomId", roomId);

                if (isBlank(chatMessage.getMessage())) {
                    chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
                }
            } else {
                // TALK인 경우에도 방 검증
                if (room == null) {
                    sendError(session, "ROOM_NOT_FOUND");
                    return;
                }
            }

            TextMessage outbound = Util.Chat.resolveTextMessage(chatMessage);
            room.sendMessage(outbound);

        } catch (Exception e) {
            log.error("handleAction error: {}", e.getMessage(), e);
            safeSend(session, "{\"error\":\"INTERNAL_ERROR\"}");
        }
    }

    /* ----------------------------- helpers ----------------------------- */

    private void sendError(WebSocketSession session, String code) {
        safeSend(session, "{\"error\":\"" + code + "\"}");
    }

    private void safeSend(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (Exception ex) {
            log.warn("Failed to send error to client: {}", ex.getMessage());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}