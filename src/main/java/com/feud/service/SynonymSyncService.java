package com.feud.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feud.model.Answer;
import com.feud.model.Question;
import com.feud.model.SynonymDictionary;
import com.feud.repository.QuestionRepository;
import com.feud.repository.SynonymDictionaryRepository;

@Service
public class SynonymSyncService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final QuestionRepository questionRepository;
    private final SynonymDictionaryRepository synonymDictionaryRepository;

    public SynonymSyncService(QuestionRepository questionRepository, SynonymDictionaryRepository synonymDictionaryRepository) {
        this.questionRepository = questionRepository;
        this.synonymDictionaryRepository = synonymDictionaryRepository;
    }

    public String fetchSynonyms(String word) {
        String url = "https://api.datamuse.com/words?rel_syn=" + word;
        String response = restTemplate.getForObject(url, String.class);
        Set<String> synonyms = new HashSet<>();
        try {
            JsonNode arr = objectMapper.readTree(response);
            for (JsonNode obj : arr) {
                synonyms.add(obj.get("word").asText());
            }
        } catch (Exception e) {
            // Optionally log error
        }
        return String.join(",", synonyms);
    }

    public Map<String, String> syncAllAnswerSynonyms() {
        List<Question> questions = questionRepository.findAll();
        Set<String> canonicalWords = new HashSet<>();
        for (Question q : questions) {
            for (Answer a : q.getAnswers()) {
                canonicalWords.add(a.getText().toLowerCase());
            }
        }
        Map<String, String> result = new HashMap<>();
        for (String word : canonicalWords) {
            // Only fetch and save if not already present
            if (synonymDictionaryRepository.findByCanonical(word).isEmpty()) {
                String synonyms = fetchSynonyms(word);
                SynonymDictionary entry = new SynonymDictionary();
                entry.setCanonical(word);
                entry.setSynonyms(synonyms);
                synonymDictionaryRepository.save(entry);
                result.put(word, synonyms);
            }
        }
        return result;
    }
}
