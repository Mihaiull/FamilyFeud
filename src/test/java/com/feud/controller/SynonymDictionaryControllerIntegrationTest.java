package com.feud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feud.model.SynonymDictionary;
import com.feud.repository.SynonymDictionaryRepository;
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

@WebMvcTest(SynonymDictionaryController.class)
public class SynonymDictionaryControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SynonymDictionaryRepository synonymDictionaryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrUpdate_returnsCreated() throws Exception {
        SynonymDictionary entry = new SynonymDictionary();
        entry.setCanonical("car");
        entry.setSynonyms("auto,vehicle");
        Mockito.when(synonymDictionaryRepository.save(any(SynonymDictionary.class))).thenReturn(entry);

        mockMvc.perform(post("/synonyms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.canonical").value("car"));
    }

    @Test
    void getAll_returnsList() throws Exception {
        SynonymDictionary entry1 = new SynonymDictionary(); entry1.setCanonical("car");
        SynonymDictionary entry2 = new SynonymDictionary(); entry2.setCanonical("bike");
        Mockito.when(synonymDictionaryRepository.findAll()).thenReturn(Arrays.asList(entry1, entry2));

        mockMvc.perform(get("/synonyms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].canonical").value("car"))
                .andExpect(jsonPath("$[1].canonical").value("bike"));
    }

    @Test
    void get_found() throws Exception {
        SynonymDictionary entry = new SynonymDictionary(); entry.setCanonical("car");
        Mockito.when(synonymDictionaryRepository.findByCanonical(eq("car"))).thenReturn(Optional.of(entry));

        mockMvc.perform(get("/synonyms/car"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonical").value("car"));
    }

    @Test
    void get_notFound() throws Exception {
        Mockito.when(synonymDictionaryRepository.findByCanonical(eq("plane"))).thenReturn(Optional.empty());

        mockMvc.perform(get("/synonyms/plane"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_found() throws Exception {
        Mockito.when(synonymDictionaryRepository.existsById(eq("car"))).thenReturn(true);
        Mockito.doNothing().when(synonymDictionaryRepository).deleteById(eq("car"));

        mockMvc.perform(delete("/synonyms/car"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound() throws Exception {
        Mockito.when(synonymDictionaryRepository.existsById(eq("plane"))).thenReturn(false);

        mockMvc.perform(delete("/synonyms/plane"))
                .andExpect(status().isNotFound());
    }
}
