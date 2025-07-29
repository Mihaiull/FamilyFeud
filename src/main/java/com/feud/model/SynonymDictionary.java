package com.feud.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SynonymDictionary {
    @Id
    private String canonical; // e.g., "car"
    private String synonyms;  // e.g., "automobile,vehicle,auto"

    public String getCanonical() { return canonical; }
    public void setCanonical(String canonical) { this.canonical = canonical; }

    public String getSynonyms() { return synonyms; }
    public void setSynonyms(String synonyms) { this.synonyms = synonyms; }
}
