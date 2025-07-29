package com.feud.dto;

import com.feud.model.Team;

public record JoinGameRequest(String name, Team team) {}
