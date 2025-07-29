package com.feud.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.feud.model.Game;

@Component
public class GameWebSocketBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameWebSocketBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastGameState(Game game) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getCode(), game);
    }
}
