package com.feud.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.feud.dto.JoinGameRequest;
import com.feud.model.Game;
import com.feud.model.GameStatus;
import com.feud.model.Player;
import com.feud.model.Team;
import com.feud.repository.GameRepository;
import com.feud.repository.PlayerRepository;
import com.feud.util.CodeGenerator;
import com.feud.websocket.GameWebSocketBroadcaster;

import jakarta.transaction.Transactional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameWebSocketBroadcaster webSocketBroadcaster;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, GameWebSocketBroadcaster webSocketBroadcaster){
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.webSocketBroadcaster = webSocketBroadcaster;
    }

    public Player joinGame(String code, JoinGameRequest request) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        Optional<Player> existing = playerRepository.findByNameAndGameCode(request.name(), code);
        if (existing.isPresent()) {
            throw new RuntimeException("This player name is already taken in this lobby");
        }
        Player player = Player.builder()
            .name(request.name())
            .team(request.team())
            .game(game)
            .build();
        Player saved = playerRepository.save(player);
        // Broadcast updated game state after player joins
        webSocketBroadcaster.broadcastGameState(gameRepository.findByCode(code).orElse(game));
        return saved;
    }

    public Game createGame(String topic) {
        String code;
        do { 
            code = CodeGenerator.generateCode();
        } while(gameRepository.existsByCode(code));

        Game game = Game.builder()
            .code(code)
            .status(GameStatus.LOBBY)
            .topic(topic)
            .build();
        Game saved = gameRepository.save(game);
        // Broadcast new game state after creation
        webSocketBroadcaster.broadcastGameState(saved);
        return saved;
    }

    public List<Player> getPlayersInGame(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        return playerRepository.findByGameCode(game.getCode());
    }

    public Game startGame(String code) {
    Game game = gameRepository.findByCode(code)
        .orElseThrow(() -> new RuntimeException("Game not found"));

    game.setStatus(GameStatus.IN_PROGRESS);
    return gameRepository.save(game);
    }

    @Transactional
    public void endGame(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));

        // Delete all players first (if cascade is not set up)
        playerRepository.deleteAll(game.getPlayers());

        // Then delete game
        gameRepository.delete(game);
    }


    // --- Game Logic Methods ---

    public Game nextRound(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        game.setRoundNumber(game.getRoundNumber() + 1);
        game.setStrikes(0);
        // Alternate starting team
        if (game.getCurrentTeam() == null || game.getCurrentTeam() == Team.BLUE) {
            game.setCurrentTeam(Team.RED);
        } else {
            game.setCurrentTeam(Team.BLUE);
        }
        return gameRepository.save(game);
    }

    public Game addStrike(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        game.setStrikes(game.getStrikes() + 1);
        return gameRepository.save(game);
    }

    public Game switchTeam(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getCurrentTeam() == null || game.getCurrentTeam() == Team.BLUE) {
            game.setCurrentTeam(Team.RED);
        } else {
            game.setCurrentTeam(Team.BLUE);
        }
        game.setStrikes(0);
        return gameRepository.save(game);
    }


    public Game addScore(String code, Team team, int points, int multiplier) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        int totalPoints = points * multiplier;
        if (team == Team.RED) {
            game.setRedScore(game.getRedScore() + totalPoints);
        } else if (team == Team.BLUE) {
            game.setBlueScore(game.getBlueScore() + totalPoints);
        }
        return gameRepository.save(game);
    }

    // --- Turn Management, Strikes, and Steal Mechanic ---

    /**
     * Submit a guess for the current team. Returns true if correct, false if not.
     * If incorrect, increments strikes. If strikes reach 3, enables steal for the other team.
     */
    public boolean submitGuess(String code, String guess, java.util.List<com.feud.model.Answer> answers) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        boolean correct = false;
        for (com.feud.model.Answer a : answers) {
            if (a.getText() != null && a.getText().equalsIgnoreCase(guess)) {
                correct = true;
                break;
            }
        }
        if (correct) {
            game.setStrikes(0); // reset strikes on correct guess
        } else {
            game.setStrikes(game.getStrikes() + 1);
        }
        gameRepository.save(game);
        return correct;
    }

    /**
     * Switches the turn to the other team and resets strikes.
     */
    public Game switchTurn(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getCurrentTeam() == Team.RED) {
            game.setCurrentTeam(Team.BLUE);
        } else {
            game.setCurrentTeam(Team.RED);
        }
        game.setStrikes(0);
        return gameRepository.save(game);
    }

    /**
     * Allows the opposing team to steal after 3 strikes. Returns true if steal is successful.
     */
    public boolean attemptSteal(String code, String guess, java.util.List<com.feud.model.Answer> answers) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getStrikes() < 3) throw new RuntimeException("Steal not allowed yet");
        boolean correct = false;
        for (com.feud.model.Answer a : answers) {
            if (a.getText() != null && a.getText().equalsIgnoreCase(guess)) {
                correct = true;
                break;
            }
        }
        // Reset strikes and switch turn after steal attempt
        game.setStrikes(0);
        switchTurn(code);
        gameRepository.save(game);
        return correct;
    }

    // --- Faceoff Logic ---

    public Game startFaceoff(String code, Long redPlayerId, Long bluePlayerId) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        game.setRedFaceoffPlayerId(redPlayerId);
        game.setBlueFaceoffPlayerId(bluePlayerId);
        game.setRedFaceoffAnswer(null);
        game.setBlueFaceoffAnswer(null);
        game.setFaceoffInProgress(true);
        return gameRepository.save(game);
    }

    public Game submitFaceoffAnswer(String code, Team team, String answer) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        if (!game.isFaceoffInProgress()) throw new RuntimeException("No faceoff in progress");
        if (team == Team.RED) {
            game.setRedFaceoffAnswer(answer);
        } else if (team == Team.BLUE) {
            game.setBlueFaceoffAnswer(answer);
        }
        return gameRepository.save(game);
    }


    /**
     * Resolves the faceoff, sets the starting team for the round, and resets faceoff state.
     * Returns the winning team (or null for tie/invalid).
     */
    public Team resolveFaceoffAndSetTurn(Game game, java.util.List<com.feud.model.Answer> answers) {
        if (!game.isFaceoffInProgress()) return null;
        String redAns = game.getRedFaceoffAnswer();
        String blueAns = game.getBlueFaceoffAnswer();
        int redPoints = getAnswerPoints(redAns, answers);
        int bluePoints = getAnswerPoints(blueAns, answers);
        Team winner = null;
        if (redPoints > bluePoints) winner = Team.RED;
        else if (bluePoints > redPoints) winner = Team.BLUE;
        // Set the starting team for the round
        game.setCurrentTeam(winner);
        // Reset faceoff state
        game.setFaceoffInProgress(false);
        game.setRedFaceoffPlayerId(null);
        game.setBlueFaceoffPlayerId(null);
        game.setRedFaceoffAnswer(null);
        game.setBlueFaceoffAnswer(null);
        gameRepository.save(game);
        return winner;
    }

    private int getAnswerPoints(String guess, java.util.List<com.feud.model.Answer> answers) {
        if (guess == null) return -1;
        for (com.feud.model.Answer a : answers) {
            // Use your answer checking logic here (e.g., AnswerCheckerService)
            if (a.getText() != null && a.getText().equalsIgnoreCase(guess)) {
                return a.getPoints();
            }
        }
        return -1;
    }

    public Game getGameByCode(String code) {
        return gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
    }
}
