
package com.feud.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 6)
    private String code;

    @Column(nullable = true)
    private String topic;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    // Other fields like players, answers, etc. can be added here
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players;

    // Current question for the round
    @ManyToOne
    @JoinColumn(name = "current_question_id")
    private Question currentQuestion;


    // Game logic fields
    private int roundNumber;
    @Builder.Default
    private int maxRounds = 3;
    @Enumerated(EnumType.STRING)
    private Team currentTeam; // RED or BLUE
    private int strikes;
    private int redScore;
    private int blueScore;

    // Track revealed answers for the current question (answer IDs)
    @jakarta.persistence.ElementCollection
    @Builder.Default
    private java.util.Set<Long> revealedAnswerIds = new java.util.HashSet<>();

    // Winner (set at game end)
    @Enumerated(EnumType.STRING)
    private Team winner;

    // Faceoff fields
    private Long redFaceoffPlayerId;
    private Long blueFaceoffPlayerId;
    private String redFaceoffAnswer;
    private String blueFaceoffAnswer;
    private boolean faceoffInProgress;
}
