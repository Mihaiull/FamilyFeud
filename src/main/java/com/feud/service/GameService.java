package com.feud.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.feud.dto.JoinGameRequest;
import com.feud.model.Game;
import com.feud.model.GameStatus;
import com.feud.model.Player;
import com.feud.model.Question;
import com.feud.model.Team;
import com.feud.repository.GameRepository;
import com.feud.repository.PlayerRepository;
import com.feud.repository.QuestionRepository;
import com.feud.util.CodeGenerator;
import com.feud.websocket.GameWebSocketBroadcaster;

import jakarta.transaction.Transactional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameWebSocketBroadcaster webSocketBroadcaster;
    private final QuestionRepository questionRepository;
    private final SynonymService synonymService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, GameWebSocketBroadcaster webSocketBroadcaster, QuestionRepository questionRepository, SynonymService synonymService){
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.webSocketBroadcaster = webSocketBroadcaster;
        this.questionRepository = questionRepository;
        this.synonymService = synonymService;
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

        // 1. Set status
        game.setStatus(GameStatus.IN_PROGRESS);
        // 2. Set round number
        game.setRoundNumber(1);
        // 3. Set strikes
        game.setStrikes(0);
        // 4. Set scores
        game.setRedScore(0);
        game.setBlueScore(0);
        // 5. Set starting team (randomly)
        game.setCurrentTeam(Math.random() < 0.5 ? Team.RED : Team.BLUE);
        // 6. Select and persist a random question for the round
        java.util.List<Question> allQuestions = questionRepository.findAll();
        if (!allQuestions.isEmpty()) {
            Question selected = allQuestions.get((int)(Math.random() * allQuestions.size()));
            game.setCurrentQuestion(selected);
            System.out.println("Selected question for game " + code + ": " + selected.getText());
        } else {
            game.setCurrentQuestion(null);
        }
        // 7. Broadcast updated state
        Game saved = gameRepository.save(game);
        webSocketBroadcaster.broadcastGameState(saved);
        return saved;
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

    /**
     * Validates that the game is in progress and not ended.
     */
    private void validateGameInProgress(Game game) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new RuntimeException("Game is not in progress");
        }
    }

    /**
     * Validates that the answer has not already been revealed.
     */
    private void validateAnswerNotRevealed(Game game, Long answerId) {
        if (game.getRevealedAnswerIds().contains(answerId)) {
            throw new RuntimeException("Answer already revealed");
        }
    }

    /**
     * Reveal an answer for the current question by ID. Adds to revealedAnswerIds.
     */
    public Game revealAnswer(String code, Long answerId) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        validateGameInProgress(game);
        validateAnswerNotRevealed(game, answerId);
        if (game.getRevealedAnswerIds() == null) {
            game.setRevealedAnswerIds(new java.util.HashSet<>());
        }
        game.getRevealedAnswerIds().add(answerId);
        return gameRepository.save(game);
    }

    /**
     * Move to the next round: increment round, reset strikes, select new question, clear revealed answers.
     * If maxRounds reached, set status to ENDED and winner.
     */
    public Game advanceToNextRound(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getRoundNumber() >= game.getMaxRounds()) {
            // End game
            game.setStatus(GameStatus.ENDED);
            if (game.getRedScore() > game.getBlueScore()) {
                game.setWinner(Team.RED);
            } else if (game.getBlueScore() > game.getRedScore()) {
                game.setWinner(Team.BLUE);
            } else {
                game.setWinner(null); // Tie
            }
            return gameRepository.save(game);
        }
        game.setRoundNumber(game.getRoundNumber() + 1);
        game.setStrikes(0);
        // Alternate starting team
        if (game.getCurrentTeam() == null || game.getCurrentTeam() == Team.BLUE) {
            game.setCurrentTeam(Team.RED);
        } else {
            game.setCurrentTeam(Team.BLUE);
        }
        // Select a new random question
        java.util.List<Question> allQuestions = questionRepository.findAll();
        if (!allQuestions.isEmpty()) {
            Question selected = allQuestions.get((int)(Math.random() * allQuestions.size()));
            game.setCurrentQuestion(selected);
        } else {
            game.setCurrentQuestion(null);
        }
        // Clear revealed answers
        game.setRevealedAnswerIds(new java.util.HashSet<>());
        return gameRepository.save(game);
    }

    /**
     * End the game immediately and set the winner.
     */
    public Game endGameAndSetWinner(String code) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        game.setStatus(GameStatus.ENDED);
        if (game.getRedScore() > game.getBlueScore()) {
            game.setWinner(Team.RED);
        } else if (game.getBlueScore() > game.getRedScore()) {
            game.setWinner(Team.BLUE);
        } else {
            game.setWinner(null); // Tie
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
     * Checks if guess matches answer text or any synonym.
     */
    private boolean isCorrectGuess(String guess, String answerText) {
        if (guess == null || answerText == null) return false;
        if (guess.equalsIgnoreCase(answerText)) return true;
        return synonymService.areSynonyms(guess, answerText);
    }

    /**
     * Awards points for revealed answers to the current team.
     * Only unrevealed answers are scored on correct guess or steal.
     */
    private void awardPointsForRevealedAnswers(Game game, java.util.List<com.feud.model.Answer> answers, Team team) {
        int points = 0;
        for (com.feud.model.Answer a : answers) {
            if (game.getRevealedAnswerIds().contains(a.getId())) {
                points += a.getPoints();
            }
        }
        if (team == Team.RED) {
            game.setRedScore(game.getRedScore() + points);
        } else if (team == Team.BLUE) {
            game.setBlueScore(game.getBlueScore() + points);
        }
    }

    /**
     * Submit a guess for the current team. Reveals answer if correct (by text or synonym), awards points, advances round if all answers revealed.
     */
    public boolean submitGuess(String code, String guess, java.util.List<com.feud.model.Answer> answers) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        validateGameInProgress(game);
        boolean correct = false;
        int pointsAwarded = 0;
        for (com.feud.model.Answer a : answers) {
            if (isCorrectGuess(guess, a.getText()) && !game.getRevealedAnswerIds().contains(a.getId())) {
                correct = true;
                game.getRevealedAnswerIds().add(a.getId()); // reveal answer automatically
                pointsAwarded += a.getPoints();
            }
        }
        if (correct) {
            game.setStrikes(0); // reset strikes on correct guess
            // Award points for this guess to current team
            if (game.getCurrentTeam() != null) {
                if (game.getCurrentTeam() == Team.RED) {
                    game.setRedScore(game.getRedScore() + pointsAwarded);
                } else if (game.getCurrentTeam() == Team.BLUE) {
                    game.setBlueScore(game.getBlueScore() + pointsAwarded);
                }
            }
        } else {
            game.setStrikes(game.getStrikes() + 1);
        }
        // If all answers revealed, advance round automatically
        boolean allRevealed = answers.stream().allMatch(ans -> game.getRevealedAnswerIds().contains(ans.getId()));
        if (allRevealed) {
            advanceToNextRound(code);
        }
        gameRepository.save(game);
        webSocketBroadcaster.broadcastGameState(game);
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
     * Attempt a steal after 3 strikes. Awards points for all revealed answers to stealing team.
     */
    public boolean attemptSteal(String code, String guess, java.util.List<com.feud.model.Answer> answers) {
        Game game = gameRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Game not found"));
        validateGameInProgress(game);
        if (game.getStrikes() < 3) throw new RuntimeException("Steal not allowed yet");
        boolean correct = false;
        int pointsAwarded = 0;
        for (com.feud.model.Answer a : answers) {
            if (isCorrectGuess(guess, a.getText()) && !game.getRevealedAnswerIds().contains(a.getId())) {
                correct = true;
                game.getRevealedAnswerIds().add(a.getId());
                pointsAwarded += a.getPoints();
            }
        }
        // Award all revealed answer points to stealing team
        Team stealingTeam = (game.getCurrentTeam() == Team.RED) ? Team.BLUE : Team.RED;
        awardPointsForRevealedAnswers(game, answers, stealingTeam);
        // Reset strikes and switch turn after steal attempt
        game.setStrikes(0);
        switchTurn(code);
        gameRepository.save(game);
        webSocketBroadcaster.broadcastGameState(game);
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

    public GameWebSocketBroadcaster getWebSocketBroadcaster() {
        return webSocketBroadcaster;
    }
}
