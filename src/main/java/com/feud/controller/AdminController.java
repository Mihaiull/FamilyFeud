package com.feud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.feud.model.Answer;
import com.feud.model.Game;
import com.feud.model.Player;
import com.feud.model.Question;
import com.feud.repository.GameRepository;
import com.feud.repository.PlayerRepository;
import com.feud.repository.QuestionRepository;
import com.feud.repository.SynonymDictionaryRepository;
import com.feud.service.SynonymService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final QuestionRepository questionRepository;
    private final SynonymService synonymService;
    private final SynonymDictionaryRepository synonymDictionaryRepository;

    public AdminController(GameRepository gameRepository, PlayerRepository playerRepository, QuestionRepository questionRepository, SynonymService synonymService, SynonymDictionaryRepository synonymDictionaryRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.questionRepository = questionRepository;
        this.synonymService = synonymService;
        this.synonymDictionaryRepository = synonymDictionaryRepository;
    }

    @DeleteMapping("/games")
    public void deleteAllGames() {
        gameRepository.deleteAll();
    }

    @DeleteMapping("/players")
    public void deleteAllPlayers() {
        playerRepository.deleteAll();
    }

    @DeleteMapping("/questions")
    public void deleteAllQuestions() {
        questionRepository.deleteAll();
    }

    @DeleteMapping("/synonyms")
    public void deleteAllSynonyms() {
        synonymDictionaryRepository.deleteAll();
    }

    @GetMapping("/games")
    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }

    @GetMapping("/questions")
    public List<Question> getQuestions() {
        return questionRepository.findAll();
    }

    @PostMapping("/questions")
    public Question addQuestion(@RequestBody Map<String, Object> body) {
        String questionText = (String) body.get("question");
        List<Map<String, Object>> answersList = (List<Map<String, Object>>) body.get("answers");
        List<Answer> answers = new ArrayList<>();
        Question question = new Question();
        question.setText(questionText);
        for (Map<String, Object> ans : answersList) {
            String text = (String) ans.get("text");
            int points = (int) ans.get("points");
            Answer answer = new Answer();
            answer.setText(text);
            answer.setPoints(points);
            answer.setRevealed(false);
            answer.setQuestion(question);
            answers.add(answer);
        }
        question.setAnswers(answers);
        return questionRepository.save(question);
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestionById(@PathVariable Long id) {
        questionRepository.deleteById(id);
    }

    @PutMapping("/questions/{id}")
    public Question updateQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Question question = questionRepository.findById(id).orElseThrow();
        String questionText = (String) body.get("question");
        List<Map<String, Object>> answersList = (List<Map<String, Object>>) body.get("answers");
        // Clear and repopulate the existing answers list
        question.getAnswers().clear();
        for (Map<String, Object> ans : answersList) {
            String text = (String) ans.get("text");
            int points = (int) ans.get("points");
            Answer answer = new Answer();
            answer.setText(text);
            answer.setPoints(points);
            answer.setRevealed(false);
            answer.setQuestion(question);
            question.getAnswers().add(answer);
        }
        question.setText(questionText);
        return questionRepository.save(question);
    }

    @PostMapping("/synonyms")
    public Map<String, List<String>> getSynonymsForAnswers(@RequestBody Map<String, List<String>> body) {
        List<String> answers = body.get("answers");
        Map<String, List<String>> result = new HashMap<>();
        for (String answer : answers) {
            Set<String> synonymsSet = synonymService.getAllSynonyms(answer);
            // Remove the canonical itself from the list, only return true synonyms
            synonymsSet.remove(answer.trim().toLowerCase());
            result.put(answer, new ArrayList<>(synonymsSet));
        }
        return result;
    }
}
