package com.odds.scoreboard.infrastructure;

import com.odds.scoreboard.domain.Match;
import com.odds.scoreboard.domain.MatchId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MatchStorage {

    private final ConcurrentMap<String, Match> storage = new ConcurrentHashMap<>();

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
        Match deleted = storage.remove(key.getId());
        if (deleted == null) {
            throw new RuntimeException("Key doesn't exist");
        }
    }

    /**
     * Returns deep copy of all matches saved in storage.
     * Any changes performed on result don't affect storage itself.
     *
     * @return List of all matches in storage
     */
    public List<Match> getAll() {
        return storage.values()
                .stream()
                .map(Match::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
