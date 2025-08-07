package com.feud.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.feud.repository.QuestionRepository;
import com.feud.repository.SynonymDictionaryRepository;

class SynonymSyncServiceTest {
    private QuestionRepository mockQuestionRepo;
    private SynonymDictionaryRepository mockSynonymRepo;
    private SynonymSyncService synonymSyncService;

    @BeforeEach
    void setUp() {
        mockQuestionRepo = Mockito.mock(QuestionRepository.class);
        mockSynonymRepo = Mockito.mock(SynonymDictionaryRepository.class);
        synonymSyncService = new SynonymSyncService(mockQuestionRepo, mockSynonymRepo);
    }

    @Test
    void testFetchSynonyms_returnsString() {
        // This test only checks that the method returns a string (integration with external API is not mocked)
        String result = synonymSyncService.fetchSynonyms("car");
        assertNotNull(result);
    }
}
