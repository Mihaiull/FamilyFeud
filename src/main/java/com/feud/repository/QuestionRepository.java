// filepath: src/main/java/com/feud/repository/QuestionRepository.java
package com.feud.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feud.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}