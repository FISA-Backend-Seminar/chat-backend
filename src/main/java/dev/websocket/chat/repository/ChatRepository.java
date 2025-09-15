package dev.websocket.chat.repository;

import dev.websocket.chat.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Repository
public class ChatRepository {

    private final Map<String, ChatRoom> chatRoomMap = new ConcurrentHashMap<>();

    public void save(String roomId, ChatRoom chatRoom) {
        chatRoomMap.put(roomId, chatRoom);
    }

    public ChatRoom findById(String roomId) {
        return chatRoomMap.get(roomId);
    }

    public List<ChatRoom> findAll() {
        ArrayList<ChatRoom> chatRooms = new ArrayList<>(chatRoomMap.values());// 저장된 모든 채팅방을 가져옴
        Collections.reverse(chatRooms); // 역순 정렬 -> 최근 생성된 채팅방이 앞쪽에 오도록 반환
        return chatRooms;
    }
}