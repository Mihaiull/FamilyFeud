package com.feud.service;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.feud.model.SynonymDictionary;
import com.feud.repository.SynonymDictionaryRepository;

class SynonymServiceTest {
    private SynonymDictionaryRepository mockRepo;
    private SynonymService synonymService;

    @BeforeEach
    void setUp() {
        mockRepo = Mockito.mock(SynonymDictionaryRepository.class);
        synonymService = new SynonymService(mockRepo);
    }

    @Test
    void testGetAllSynonyms_withEntry() {
        SynonymDictionary entry = new SynonymDictionary();
        entry.setCanonical("car");
        entry.setSynonyms("auto,vehicle");
        Mockito.when(mockRepo.findByCanonical("car")).thenReturn(Optional.of(entry));
        Set<String> result = synonymService.getAllSynonyms("car");
        assertTrue(result.contains("car"));
        assertTrue(result.contains("auto"));
        assertTrue(result.contains("vehicle"));
    }

    @Test
    void testGetAllSynonyms_noEntry() {
        Mockito.when(mockRepo.findByCanonical("plane")).thenReturn(Optional.empty());
        Set<String> result = synonymService.getAllSynonyms("plane");
        assertTrue(result.contains("plane"));
        assertEquals(1, result.size());
    }

    @Test
    void testAreSynonyms_true() {
        SynonymDictionary entry = new SynonymDictionary();
        entry.setCanonical("car");
        entry.setSynonyms("auto,vehicle");
        Mockito.when(mockRepo.findByCanonical("car")).thenReturn(Optional.of(entry));
        Set<String> result = synonymService.getAllSynonyms("car");
        assertTrue(synonymService.areSynonyms("car", "auto"));
    }

    @Test
    void testAreSynonyms_false() {
        Mockito.when(mockRepo.findByCanonical("car")).thenReturn(Optional.empty());
        Mockito.when(mockRepo.findByCanonical("plane")).thenReturn(Optional.empty());
        assertFalse(synonymService.areSynonyms("car", "plane"));
    }
}
