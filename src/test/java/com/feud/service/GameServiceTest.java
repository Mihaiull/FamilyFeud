package com.feud.service;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.feud.model.Answer;
import com.feud.model.Game;
import com.feud.model.Team;

class GameServiceTest {
    private GameService gameService;
    private com.feud.repository.GameRepository mockGameRepository;
    private com.feud.websocket.GameWebSocketBroadcaster mockBroadcaster;
    private com.feud.repository.QuestionRepository mockQuestionRepository;
    private SynonymService mockSynonymService;

    @BeforeEach
    void setUp() {
        mockGameRepository = Mockito.mock(com.feud.repository.GameRepository.class);
        mockBroadcaster = Mockito.mock(com.feud.websocket.GameWebSocketBroadcaster.class);
        mockQuestionRepository = Mockito.mock(com.feud.repository.QuestionRepository.class);
        mockSynonymService = Mockito.mock(com.feud.service.SynonymService.class);
        // Save just returns the game object
        Mockito.when(mockGameRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        gameService = new GameService(mockGameRepository, null, mockBroadcaster, mockQuestionRepository, mockSynonymService);
    }

    @Test
    void testResolveFaceoffAndSetTurn_redWins() {
        Game game = new Game();
        game.setFaceoffInProgress(true);
        game.setRedFaceoffAnswer("Car");
        game.setBlueFaceoffAnswer("Bike");
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build(),
                Answer.builder().text("Bike").points(20).build()
        );
        Team winner = gameService.resolveFaceoffAndSetTurn(game, answers);
        assertEquals(Team.RED, winner);
        assertEquals(Team.RED, game.getCurrentTeam());
        assertFalse(game.isFaceoffInProgress());
    }

    @Test
    void testResolveFaceoffAndSetTurn_blueWins() {
        Game game = new Game();
        game.setFaceoffInProgress(true);
        game.setRedFaceoffAnswer("Box");
        game.setBlueFaceoffAnswer("Car");
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build(),
                Answer.builder().text("Box").points(10).build()
        );
        Team winner = gameService.resolveFaceoffAndSetTurn(game, answers);
        assertEquals(Team.BLUE, winner);
        assertEquals(Team.BLUE, game.getCurrentTeam());
        assertFalse(game.isFaceoffInProgress());
    }

    @Test
    void testResolveFaceoffAndSetTurn_tie() {
        Game game = new Game();
        game.setFaceoffInProgress(true);
        game.setRedFaceoffAnswer("Car");
        game.setBlueFaceoffAnswer("Car");
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build()
        );
        Team winner = gameService.resolveFaceoffAndSetTurn(game, answers);
        assertNull(winner);
        assertNull(game.getCurrentTeam());
        assertFalse(game.isFaceoffInProgress());
    }
    @Test
    void testSubmitGuess_correctResetsStrikes() {
        Game game = new Game();
        game.setStrikes(2);
        game.setStatus(com.feud.model.GameStatus.IN_PROGRESS);
        Mockito.when(mockGameRepository.findByCode("CODE")).thenReturn(java.util.Optional.of(game));
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build()
        );
        boolean result = gameService.submitGuess("CODE", "Car", answers);
        assertEquals(true, result);
        assertEquals(0, game.getStrikes());
    }

    @Test
    void testSubmitGuess_incorrectIncrementsStrikes() {
        Game game = new Game();
        game.setStrikes(1);
        game.setStatus(com.feud.model.GameStatus.IN_PROGRESS);
        Mockito.when(mockGameRepository.findByCode("CODE")).thenReturn(java.util.Optional.of(game));
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build()
        );
        boolean result = gameService.submitGuess("CODE", "Bike", answers);
        assertEquals(false, result);
        assertEquals(2, game.getStrikes());
    }

    @Test
    void testSwitchTurn() {
        Game game = new Game();
        game.setCurrentTeam(Team.RED);
        game.setStrikes(2);
        Mockito.when(mockGameRepository.findByCode("CODE")).thenReturn(java.util.Optional.of(game));
        Game updated = gameService.switchTurn("CODE");
        assertEquals(Team.BLUE, updated.getCurrentTeam());
        assertEquals(0, updated.getStrikes());
    }

    @Test
    void testAttemptSteal_successful() {
        Game game = new Game();
        game.setStrikes(3);
        game.setStatus(com.feud.model.GameStatus.IN_PROGRESS);
        Mockito.when(mockGameRepository.findByCode("CODE")).thenReturn(java.util.Optional.of(game));
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build()
        );
        boolean result = gameService.attemptSteal("CODE", "Car", answers);
        assertEquals(true, result);
        assertEquals(0, game.getStrikes());
    }

    @Test
    void testAttemptSteal_unsuccessful() {
        Game game = new Game();
        game.setStrikes(3);
        game.setStatus(com.feud.model.GameStatus.IN_PROGRESS);
        Mockito.when(mockGameRepository.findByCode("CODE")).thenReturn(java.util.Optional.of(game));
        List<Answer> answers = Arrays.asList(
                Answer.builder().text("Car").points(40).build()
        );
        boolean result = gameService.attemptSteal("CODE", "Bike", answers);
        assertEquals(false, result);
        assertEquals(0, game.getStrikes());
    }
}
