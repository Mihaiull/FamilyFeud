package com.feud.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.feud.dto.CreateGameRequest;
import com.feud.dto.JoinGameRequest;
import com.feud.model.Answer;
import com.feud.model.Game;
import com.feud.model.Player;
import com.feud.model.Team;
import com.feud.service.GameService;

@RestController
@RequestMapping("/games")
@CrossOrigin(origins = "*") // Allow all origins for simplicity; adjust as needed
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Join a game with a player name and team.
     * @param code Game code
     * @param request JoinGameRequest with player name and team
     * @return The created Player or 409 if name is taken
     */
    @PostMapping("/{code}/join")
    public ResponseEntity<Player> joinGame(
            @PathVariable String code, 
            @RequestBody JoinGameRequest request) {
        try {
            Player player = gameService.joinGame(code, request);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Create a new game with a topic.
     * @param request CreateGameRequest with topic
     * @return The created Game
     */
    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody CreateGameRequest request) {
        Game newGame = gameService.createGame(request.topic());
        return ResponseEntity.ok(newGame);
    }

    /**
     * Get all players in a game.
     * @param code Game code
     * @return List of players or 404 if game not found
     */
    @GetMapping("/{code}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable String code) {
        try {
            List<Player> players = gameService.getPlayersInGame(code);
            return ResponseEntity.ok(players);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Start a game (move to IN_PROGRESS).
     * @param code Game code
     * @return The updated Game
     */
    @PostMapping("/{code}/start")
    public ResponseEntity<Game> startGame(@PathVariable String code) {
        Game game = gameService.startGame(code);
        return ResponseEntity.ok(game);
    }

    /**
     * End a game and delete all players.
     * @param code Game code
     * @return 204 No Content
     */
    @PostMapping("/{code}/end")
    public ResponseEntity<Void> endGame(@PathVariable String code) {
        gameService.endGame(code);
        return ResponseEntity.noContent().build();
    }

        // --- Faceoff Endpoints ---

    /**
     * Start a faceoff round by specifying the RED and BLUE team players.
     * @param code Game code
     * @param redPlayerId Player ID for RED
     * @param bluePlayerId Player ID for BLUE
     * @return The updated Game
     */
    @PostMapping("/{code}/faceoff/start")
    public ResponseEntity<Game> startFaceoff(@PathVariable String code, @RequestParam Long redPlayerId, @RequestParam Long bluePlayerId) {
        Game game = gameService.startFaceoff(code, redPlayerId, bluePlayerId);
        return ResponseEntity.ok(game);
    }

    /**
     * Submit a faceoff answer for a team.
     * @param code Game code
     * @param team Team (RED or BLUE)
     * @param answer The answer string
     * @return The updated Game
     */
    @PostMapping("/{code}/faceoff/answer")
    public ResponseEntity<Game> submitFaceoffAnswer(@PathVariable String code, @RequestParam Team team, @RequestParam String answer) {
        Game game = gameService.submitFaceoffAnswer(code, team, answer);
        return ResponseEntity.ok(game);
    }

    /**
     * Resolve the faceoff and set the starting team for the round.
     * @param code Game code
     * @param answers List of possible answers for the question
     * @return The winning Team (RED, BLUE, or null for tie)
     */
    @PostMapping("/{code}/faceoff/resolve")
    public ResponseEntity<Team> resolveFaceoff(@PathVariable String code, @RequestBody List<Answer> answers) {
        Game game = gameService.getGameByCode(code);
        Team winner = gameService.resolveFaceoffAndSetTurn(game, answers);
        return ResponseEntity.ok(winner);
    }

    /**
     * Get the current state of the game, including faceoff and turn info.
     * @param code Game code
     * @return The Game object
     */
    @GetMapping("/{code}/state")
    public ResponseEntity<Game> getGameState(@PathVariable String code) {
        Game game = gameService.getGameByCode(code);
        return ResponseEntity.ok(game);
    }
    /**
     * Submit a guess for the current team. Returns true if correct, false if not.
     * @param code Game code
     * @param guess The guess string
     * @param answers List of possible answers for the question
     * @return true if guess is correct, false otherwise
     */
    @PostMapping("/{code}/guess")
    public ResponseEntity<Boolean> submitGuess(@PathVariable String code, @RequestParam String guess, @RequestBody List<Answer> answers) {
        boolean correct = gameService.submitGuess(code, guess, answers);
        return ResponseEntity.ok(correct);
    }

    /**
     * Switch the turn to the other team and reset strikes.
     * @param code Game code
     * @return The updated Game
     */
    @PostMapping("/{code}/turn/switch")
    public ResponseEntity<Game> switchTurn(@PathVariable String code) {
        Game game = gameService.switchTurn(code);
        return ResponseEntity.ok(game);
    }

    /**
     * Attempt a steal after 3 strikes. Returns true if successful.
     * @param code Game code
     * @param guess The guess string
     * @param answers List of possible answers for the question
     * @return true if steal is successful, false otherwise
     */
    @PostMapping("/{code}/steal")
    public ResponseEntity<Boolean> attemptSteal(@PathVariable String code, @RequestParam String guess, @RequestBody List<Answer> answers) {
        boolean correct = gameService.attemptSteal(code, guess, answers);
        return ResponseEntity.ok(correct);
    }

}
