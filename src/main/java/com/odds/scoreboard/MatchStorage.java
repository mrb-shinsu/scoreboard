package com.odds.scoreboard;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MatchStorage {

    private ConcurrentMap<String, Match> storage = new ConcurrentHashMap<>();

    public void save(MatchId key, Match value) {
        Match existingValue = storage.putIfAbsent(key.getId(), value);
        if (existingValue != null) {
            throw new RuntimeException("Key already exists");
        }
    }

    public void update(MatchId key, Match value) {
        if (!storage.containsKey(key.getId())) {
            throw new RuntimeException("Key doesn't exist");
        }

        storage.merge(key.getId(), value,
                (oldMatch, newMatch) -> {
                    oldMatch.setHomeTeamScore(newMatch.getHomeTeamScore());
                    oldMatch.setAwayTeamScore(newMatch.getAwayTeamScore());
                    return oldMatch;
                }
        );
    }

    public void delete(MatchId key) {
        storage.remove(key.getId());
    }

    public List<Match> getAll() {
        return null;
    }
}
