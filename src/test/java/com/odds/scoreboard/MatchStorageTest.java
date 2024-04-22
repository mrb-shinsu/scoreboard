package com.odds.scoreboard;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchStorageTest {

    @Test
    public void saveIfEmptyStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
        String homeTeam = "Mexico", awayTeam = "Canada";
        MatchId expectedKey = new MatchId(homeTeam, awayTeam);
        OffsetDateTime expectedStartTime = OffsetDateTime.now();
        Match expectedMatch = new Match(homeTeam, 0, awayTeam, 0, expectedStartTime);

        MatchStorage matchStorage = new MatchStorage();
        matchStorage.save(expectedKey, expectedMatch);

        ConcurrentMap<String, Match> expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(expectedKey.getId(), expectedMatch);

        ConcurrentMap<String, Match> actualStorage = extractStorage(matchStorage);
        assertEquals(expectedStorage, actualStorage);
    }

    private ConcurrentMap<String, Match> extractStorage(MatchStorage matchStorage) throws NoSuchFieldException, IllegalAccessException {
        Field map = MatchStorage.class.getDeclaredField("storage");
        map.setAccessible(true);

        return (ConcurrentMap<String, Match>) map.get(matchStorage);
    }
}
