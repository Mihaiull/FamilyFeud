package com.feud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feud.model.Question;
import com.feud.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
public class QuestionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createQuestion_returnsCreated() throws Exception {
        Question q = new Question();
        q.setId(1L);
        q.setText("Sample?");
        Mockito.when(questionService.createQuestion(any(Question.class))).thenReturn(q);

        mockMvc.perform(post("/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(q)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getAllQuestions_returnsList() throws Exception {
        Question q1 = new Question(); q1.setId(1L); q1.setText("Q1");
        Question q2 = new Question(); q2.setId(2L); q2.setText("Q2");
        Mockito.when(questionService.getAllQuestions()).thenReturn(Arrays.asList(q1, q2));

        mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getQuestion_found() throws Exception {
        Question q = new Question(); q.setId(1L); q.setText("Q1");
        Mockito.when(questionService.getQuestion(eq(1L))).thenReturn(Optional.of(q));

        mockMvc.perform(get("/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getQuestion_notFound() throws Exception {
        Mockito.when(questionService.getQuestion(eq(99L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/questions/99"))
                .andExpect(status().isNotFound());
    }
}
