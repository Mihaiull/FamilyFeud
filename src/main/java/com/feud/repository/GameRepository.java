package com.feud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feud.model.Game;

public interface GameRepository extends JpaRepository<Game, Long>{
    Optional<Game> findByCode(String code);
    boolean existsByCode(String code);
}
