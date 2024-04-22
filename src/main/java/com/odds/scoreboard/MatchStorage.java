package com.odds.scoreboard;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MatchStorage {

    private ConcurrentMap<String, Match> storage = new ConcurrentHashMap<>();

    public void save(MatchId key, Match value) {
        storage.put(key.getId(), value);
    }

    public void update(MatchId key, Match value) {
    }

    public void delete(MatchId expectedKey) {
    }

    public List<Match> getAll() {
        return null;
    }
}
