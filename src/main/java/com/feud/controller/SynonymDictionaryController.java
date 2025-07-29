package com.feud.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.feud.model.SynonymDictionary;
import com.feud.repository.SynonymDictionaryRepository;

@RestController
@RequestMapping("/synonyms")
@CrossOrigin(origins = "*")
public class SynonymDictionaryController {
    private final SynonymDictionaryRepository synonymDictionaryRepository;

    public SynonymDictionaryController(SynonymDictionaryRepository synonymDictionaryRepository) {
        this.synonymDictionaryRepository = synonymDictionaryRepository;
    }

    /**
     * Create or update a synonym dictionary entry.
     * @param entry The SynonymDictionary object to create or update
     * @return The saved SynonymDictionary with 201 status
     */
    @PostMapping
    public ResponseEntity<SynonymDictionary> createOrUpdate(@RequestBody SynonymDictionary entry) {
        SynonymDictionary saved = synonymDictionaryRepository.save(entry);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Get all synonym dictionary entries.
     * @return List of all SynonymDictionary objects
     */
    @GetMapping
    public List<SynonymDictionary> getAll() {
        return synonymDictionaryRepository.findAll();
    }

    /**
     * Get a synonym dictionary entry by its canonical word.
     * @param canonical The canonical word
     * @return The SynonymDictionary if found, or 404 if not
     */
    @GetMapping("/{canonical}")
    public ResponseEntity<SynonymDictionary> get(@PathVariable String canonical) {
        Optional<SynonymDictionary> entry = synonymDictionaryRepository.findByCanonical(canonical);
        return entry.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a synonym dictionary entry by its canonical word.
     * @param canonical The canonical word
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{canonical}")
    public ResponseEntity<Void> delete(@PathVariable String canonical) {
        if (synonymDictionaryRepository.existsById(canonical)) {
            synonymDictionaryRepository.deleteById(canonical);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
