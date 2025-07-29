package com.feud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feud.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Additional query methods can be defined here if needed 

    Optional<Player> findByNameAndGameCode(String name, String code);
    List<Player> findByGameCode(String code);
    
}
