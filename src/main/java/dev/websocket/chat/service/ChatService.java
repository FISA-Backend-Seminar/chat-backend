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
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public List<ChatRoom> findAll() {
        return chatRepository.findAll();
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRepository.findById(roomId);
    }

    public ChatRoom createRoom(String roomId) {
        ChatRoom room = findRoomById(roomId); // ← 여기서 메서드 재사용
        if (room == null) {
            String defaultName = "room-" + roomId; // name 안 쓸 거면 roomId로 해도 OK
            room = ChatRoom.of(roomId, defaultName);
            chatRepository.save(roomId, room);
            log.info("Created room: {} ({})", roomId, defaultName);
        }
        return room;
    }

    /**
     * 메시지 액션 처리 (ENTER / TALK)
     */
    public void handleAction(String roomId, WebSocketSession session, ChatMessage chatMessage) {
        try {
            if (roomId == null || roomId.isBlank()) {
                sendError(session, "ROOM_ID_REQUIRED");
                return;
            }

            // 특정 채팅방을 구독할 때
            if (chatMessage.getMessageType().equals(ChatMessage.MessageType.ENTER)) {
                // 없으면 기본 이름으로 생성 (name을 전혀 쓰지 않는다면 roomId로 대체 가능)
                ChatRoom enterRoom = createRoom(roomId);
                enterRoom.join(session);
                session.getAttributes().put("roomId", roomId);

                if (isBlank(chatMessage.getMessage())) {
                    chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
                }
            }

            ChatRoom room = chatRepository.findById(roomId); // 클라이언트가 보낸 아이디가 있는 방이 있는지 확인

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