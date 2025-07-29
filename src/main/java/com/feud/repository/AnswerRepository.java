package com.feud.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feud.model.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // Additional query methods can be defined here if needed
}
