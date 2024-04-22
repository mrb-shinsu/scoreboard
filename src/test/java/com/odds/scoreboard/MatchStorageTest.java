package com.odds.scoreboard;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatchStorageTest {

    private static final String MEXICO = "Mexico";
    private static final String CANADA = "Canada";
    private static final String SPAIN = "Spain";
    private static final String BRAZIL = "Brazil";


    @Test
    public void saveIfEmptyStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
        var expectedKey = new MatchId(MEXICO, CANADA);
        var expectedMatch = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var matchStorage = new MatchStorage();
        matchStorage.save(expectedKey, expectedMatch);

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(expectedKey.getId(), expectedMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    public void saveIfNotEmptyStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
        var expectedKey = new MatchId(MEXICO, CANADA);
        var expectedMatch = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var existingKey = new MatchId(SPAIN, BRAZIL);
        var existingMatch = new Match(SPAIN, 1, BRAZIL, 2, OffsetDateTime.now());

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(existingKey.getId(), existingMatch));

        matchStorage.save(expectedKey, expectedMatch);

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(expectedKey.getId(), expectedMatch);
        expectedStorage.put(existingKey.getId(), existingMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    public void saveIfKeyExistsThrowException() throws NoSuchFieldException, IllegalAccessException {
        var key = new MatchId(MEXICO, CANADA);
        var existingMatch = new Match(MEXICO, 0, CANADA, 5, OffsetDateTime.now());
        var newMatch = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key.getId(), existingMatch));

        var e = assertThrows(RuntimeException.class, () -> matchStorage.save(key, newMatch));

        var actualMessage = e.getMessage();
        assertTrue(actualMessage.contains("Key already exists"));

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(key.getId(), existingMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    private ConcurrentMap<String, Match> extractStorage(MatchStorage matchStorage) throws NoSuchFieldException, IllegalAccessException {
        var map = MatchStorage.class.getDeclaredField("storage");
        map.setAccessible(true);

        return (ConcurrentMap<String, Match>) map.get(matchStorage);
    }

    private void initStorage(MatchStorage matchStorage, Map<String, Match> initialState) throws NoSuchFieldException, IllegalAccessException {
        var map = MatchStorage.class.getDeclaredField("storage");
        map.setAccessible(true);

        var storage = (ConcurrentMap<String, Match>) map.get(matchStorage);
        storage.putAll(initialState);
    }
}
