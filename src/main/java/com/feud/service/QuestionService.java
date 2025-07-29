package com.feud.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.feud.model.Question;
import com.feud.repository.QuestionRepository;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestion(Long id) {
        return questionRepository.findById(id);
    }
}
