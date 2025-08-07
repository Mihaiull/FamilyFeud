package com.feud.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.feud.model.Question;
import com.feud.repository.QuestionRepository;

class QuestionServiceTest {
    private QuestionRepository mockRepo;
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        mockRepo = Mockito.mock(QuestionRepository.class);
        questionService = new QuestionService(mockRepo);
    }

    @Test
    void testCreateQuestion() {
        Question q = new Question();
        Mockito.when(mockRepo.save(q)).thenReturn(q);
        Question result = questionService.createQuestion(q);
        assertEquals(q, result);
    }

    @Test
    void testGetAllQuestions() {
        Question q1 = new Question();
        Question q2 = new Question();
        Mockito.when(mockRepo.findAll()).thenReturn(Arrays.asList(q1, q2));
        List<Question> result = questionService.getAllQuestions();
        assertEquals(2, result.size());
        assertTrue(result.contains(q1));
        assertTrue(result.contains(q2));
    }

    @Test
    void testGetQuestion_found() {
        Question q = new Question();
        Mockito.when(mockRepo.findById(1L)).thenReturn(Optional.of(q));
        Optional<Question> result = questionService.getQuestion(1L);
        assertTrue(result.isPresent());
        assertEquals(q, result.get());
    }

    @Test
    void testGetQuestion_notFound() {
        Mockito.when(mockRepo.findById(2L)).thenReturn(Optional.empty());
        Optional<Question> result = questionService.getQuestion(2L);
        assertFalse(result.isPresent());
    }
}
