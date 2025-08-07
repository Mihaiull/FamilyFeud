package com.feud.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.feud.model.Answer;

class AnswerCheckerServiceTest {
    private SynonymService mockSynonymService;
    private AnswerCheckerService answerCheckerService;

    @BeforeEach
    void setUp() {
        mockSynonymService = Mockito.mock(SynonymService.class);
        answerCheckerService = new AnswerCheckerService(mockSynonymService);
    }

    @Test
    void testMatches_true() {
        Answer answer = Answer.builder().text("car").build();
        Mockito.when(mockSynonymService.areSynonyms("car", "auto")).thenReturn(true);
        assertTrue(answerCheckerService.matches(answer, "auto"));
    }

    @Test
    void testMatches_false() {
        Answer answer = Answer.builder().text("car").build();
        Mockito.when(mockSynonymService.areSynonyms("car", "plane")).thenReturn(false);
        assertFalse(answerCheckerService.matches(answer, "plane"));
    }

    @Test
    void testMatches_nulls() {
        assertFalse(answerCheckerService.matches(null, "car"));
        assertFalse(answerCheckerService.matches(Answer.builder().text(null).build(), "car"));
        assertFalse(answerCheckerService.matches(Answer.builder().text("car").build(), null));
    }
}
