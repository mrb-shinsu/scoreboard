package com.odds.scoreboard.infrastructure;

import com.odds.scoreboard.domain.Match;
import com.odds.scoreboard.domain.MatchId;
import com.odds.scoreboard.infrastructure.exception.KeyExistsException;
import com.odds.scoreboard.infrastructure.exception.KeyNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Thread-safe storage for {@code Match} objects.
 * It's a key-value based, using {@code ConcurrentHashMap} as internal storage.
 * Key is based on playing team names, meaning it can store only one match between same teams at the same time.
 * <p>
 * Since {@code ConcurrentHashMap} is used, this storage obeys the same concurrency specification and if any additional
 * locking or synchronization is expected, it must be done externally.
 */
public class MatchStorage {

    private final ConcurrentMap<String, Match> storage = new ConcurrentHashMap<>();

    /**
     * Save {@code Match} for certain {@code MatchId}.
     * <p>
     * If {@code key} already exists in storage, {@code KeyExistsException} is thrown.
     *
     * @param key   match identifier, not null
     * @param value match to save, not null
     */
    public void save(MatchId key, Match value) {
        Match existingValue = storage.putIfAbsent(key.getId(), value);
        if (existingValue != null) {
            throw new KeyExistsException();
        }
    }

    /**
     * Update {@code Match} for certain {@code MatchId}.
     * <p>
     * If {@code key} doesn't exist in storage, {@code KeyNotFoundException} is thrown.
     *
     * @param key   match identifier, not null
     * @param value match to update, not null
     */
    public void update(MatchId key, Match value) {
        if (!storage.containsKey(key.getId())) {
            throw new KeyNotFoundException();
        }

        storage.merge(key.getId(), value,
                (oldMatch, newMatch) -> {
                    oldMatch.setHomeTeamScore(newMatch.getHomeTeamScore());
                    oldMatch.setAwayTeamScore(newMatch.getAwayTeamScore());
                    return oldMatch;
                }
        );
    }

    /**
     * Delete {@code Match} under specific {@code key}.
     * <p>
     * If {@code key} doesn't exist in storage, {@code KeyNotFoundException} is thrown.
     *
     * @param key match identifier to delete, not null
     */
    public void delete(MatchId key) {
        Match deleted = storage.remove(key.getId());
        if (deleted == null) {
            throw new KeyNotFoundException();
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
