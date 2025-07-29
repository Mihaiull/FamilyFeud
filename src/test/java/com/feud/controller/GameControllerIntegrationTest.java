package com.feud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feud.model.Player;
import com.feud.model.Game;
import com.feud.model.Team;
import com.feud.dto.CreateGameRequest;
import com.feud.dto.JoinGameRequest;
import com.feud.service.GameService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGame_returnsGame() throws Exception {
        Game game = new Game();
        game.setCode("ABC123");
        Mockito.when(gameService.createGame(eq("General"))).thenReturn(game);
        CreateGameRequest req = new CreateGameRequest("General");
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void joinGame_returnsPlayer() throws Exception {
        Player player = new Player();
        player.setId(1L);
        player.setName("Alice");
        player.setTeam(Team.RED);
        Mockito.when(gameService.joinGame(eq("ABC123"), any(JoinGameRequest.class))).thenReturn(player);
        JoinGameRequest req = new JoinGameRequest("Alice", Team.RED);
        mockMvc.perform(post("/games/ABC123/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getPlayers_returnsList() throws Exception {
        Player p1 = new Player(); p1.setId(1L); p1.setName("A");
        Player p2 = new Player(); p2.setId(2L); p2.setName("B");
        Mockito.when(gameService.getPlayersInGame(eq("ABC123"))).thenReturn(Arrays.asList(p1, p2));
        mockMvc.perform(get("/games/ABC123/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void startGame_returnsGame() throws Exception {
        Game game = new Game(); game.setCode("ABC123");
        Mockito.when(gameService.startGame(eq("ABC123"))).thenReturn(game);
        mockMvc.perform(post("/games/ABC123/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void endGame_returnsNoContent() throws Exception {
        Mockito.doNothing().when(gameService).endGame(eq("ABC123"));
        mockMvc.perform(post("/games/ABC123/end"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getGameState_returnsGame() throws Exception {
        Game game = new Game(); game.setCode("ABC123");
        Mockito.when(gameService.getGameByCode(eq("ABC123"))).thenReturn(game);
        mockMvc.perform(get("/games/ABC123/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void startFaceoff_returnsGame() throws Exception {
        Game game = new Game();
        game.setCode("ABC123");
        Mockito.when(gameService.startFaceoff(eq("ABC123"), eq(1L), eq(2L))).thenReturn(game);
        mockMvc.perform(post("/games/ABC123/faceoff/start")
                .param("redPlayerId", "1")
                .param("bluePlayerId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void submitFaceoffAnswer_returnsGame() throws Exception {
        Game game = new Game();
        game.setCode("ABC123");
        Mockito.when(gameService.submitFaceoffAnswer(eq("ABC123"), eq(Team.RED), eq("answer"))).thenReturn(game);
        mockMvc.perform(post("/games/ABC123/faceoff/answer")
                .param("team", "RED")
                .param("answer", "answer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void resolveFaceoff_returnsTeam() throws Exception {
        Game game = new Game();
        game.setCode("ABC123");
        Mockito.when(gameService.getGameByCode(eq("ABC123"))).thenReturn(game);
        Mockito.when(gameService.resolveFaceoffAndSetTurn(eq(game), any())).thenReturn(Team.RED);
        mockMvc.perform(post("/games/ABC123/faceoff/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"RED\""));
    }

    @Test
    void submitGuess_returnsBoolean() throws Exception {
        Mockito.when(gameService.submitGuess(eq("ABC123"), eq("guess"), any())).thenReturn(true);
        mockMvc.perform(post("/games/ABC123/guess")
                .param("guess", "guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void switchTurn_returnsGame() throws Exception {
        Game game = new Game();
        game.setCode("ABC123");
        Mockito.when(gameService.switchTurn(eq("ABC123"))).thenReturn(game);
        mockMvc.perform(post("/games/ABC123/turn/switch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void attemptSteal_returnsBoolean() throws Exception {
        Mockito.when(gameService.attemptSteal(eq("ABC123"), eq("guess"), any())).thenReturn(true);
        mockMvc.perform(post("/games/ABC123/steal")
                .param("guess", "guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
