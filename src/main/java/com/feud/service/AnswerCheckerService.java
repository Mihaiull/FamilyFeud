package com.feud.service;

import org.springframework.stereotype.Service;

import com.feud.model.Answer;

@Service
public class AnswerCheckerService {
    private final SynonymService synonymService;

    public AnswerCheckerService(SynonymService synonymService) {
        this.synonymService = synonymService;
    }

    /**
     * Checks if the guess matches the answer text or any of its synonyms (global).
     */
    public boolean matches(Answer answer, String guess) {
        if (guess == null || answer == null || answer.getText() == null) return false;
        return synonymService.areSynonyms(answer.getText(), guess);
    }
}
