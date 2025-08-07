package com.feud.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.feud.service.SynonymSyncService;

@RestController
@RequestMapping("/synonyms")
@CrossOrigin(origins = "*") // Allow CORS for all origins
public class SynonymSyncController {
    private final SynonymSyncService synonymSyncService;

    public SynonymSyncController(SynonymSyncService synonymSyncService) {
        this.synonymSyncService = synonymSyncService;
    }

    @PostMapping("/sync")
    public Map<String, String> syncAll() {
        return synonymSyncService.syncAllAnswerSynonyms();
    }
}
