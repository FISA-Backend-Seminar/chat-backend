package dev.websocket.base;

import dev.websocket.chat.dto.ChatMessage;
import dev.websocket.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final ChatService chatService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        ChatMessage chatMessage = Util.Chat.resolvePayload(payload);
        chatService.handleAction(chatMessage.getRoomId(), session, chatMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션이 속한 방 찾아서 제거
        chatService.handleDisconnect(session);
    }

    // 이 코드 추가했더니 OPEN이 됨
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // CORS 켜두기
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**", "/chat/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**", "/chat/**").permitAll()
                        //.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // (선택) preflight 허용
                        .anyRequest().permitAll()
                );
        return http.build();
    }

}