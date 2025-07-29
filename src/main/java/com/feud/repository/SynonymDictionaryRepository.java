package com.feud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feud.model.SynonymDictionary;

public interface SynonymDictionaryRepository extends JpaRepository<SynonymDictionary, String> {
    Optional<SynonymDictionary> findByCanonical(String canonical);
}
