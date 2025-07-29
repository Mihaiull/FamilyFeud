package com.feud.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.feud.model.SynonymDictionary;
import com.feud.repository.SynonymDictionaryRepository;

@Service
public class SynonymService {
    private final SynonymDictionaryRepository synonymDictionaryRepository;

    public SynonymService(SynonymDictionaryRepository synonymDictionaryRepository) {
        this.synonymDictionaryRepository = synonymDictionaryRepository;
    }

    public Set<String> getAllSynonyms(String word) {
        Set<String> result = new HashSet<>();
        if (word == null) return result;
        String normalized = word.trim().toLowerCase();
        result.add(normalized);
        Optional<SynonymDictionary> entry = synonymDictionaryRepository.findByCanonical(normalized);
        if (entry.isPresent()) {
            String[] syns = entry.get().getSynonyms().split(",");
            for (String syn : syns) {
                result.add(syn.trim().toLowerCase());
            }
        }
        return result;
    }

    public boolean areSynonyms(String word1, String word2) {
        Set<String> syns1 = getAllSynonyms(word1);
        Set<String> syns2 = getAllSynonyms(word2);
        for (String s : syns1) {
            if (syns2.contains(s)) return true;
        }
        return false;
    }
}
