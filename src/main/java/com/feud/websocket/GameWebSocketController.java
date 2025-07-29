package com.feud.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.feud.model.Game;

@Controller
public class GameWebSocketController {
    // This method is just a placeholder for client-initiated messages if needed
    @MessageMapping("/game/update")
    @SendTo("/topic/game")
    public Game broadcastGame(Game game) {
        return game;
    }
}
