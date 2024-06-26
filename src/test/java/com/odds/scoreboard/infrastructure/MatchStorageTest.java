package com.odds.scoreboard.infrastructure;

import com.odds.scoreboard.BaseTest;
import com.odds.scoreboard.domain.Match;
import com.odds.scoreboard.domain.MatchId;
import com.odds.scoreboard.infrastructure.exception.KeyExistsException;
import com.odds.scoreboard.infrastructure.exception.KeyNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchStorageTest extends BaseTest {
    @Test
    void saveIfEmptyStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
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
    void saveIfNotEmptyStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
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
    void saveIfKeyExistsThrowException() throws NoSuchFieldException, IllegalAccessException {
        var key = new MatchId(MEXICO, CANADA);
        var existingMatch = new Match(MEXICO, 0, CANADA, 5, OffsetDateTime.now());
        var newMatch = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key.getId(), existingMatch));

        assertThrows(KeyExistsException.class, () -> matchStorage.save(key, newMatch));

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(key.getId(), existingMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void updateIfSingleElemInStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
        var key = new MatchId(MEXICO, CANADA);
        var startTime = OffsetDateTime.now();
        var existingMatch = new Match(MEXICO, 0, CANADA, 0, startTime);
        var newMatch = new Match(MEXICO, 0, CANADA, 1);

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key.getId(), existingMatch));

        matchStorage.update(key, newMatch);

        var expectedStorage = new ConcurrentHashMap<>();
        var expectedMatch = new Match(MEXICO, 0, CANADA, 1, startTime);
        expectedStorage.put(key.getId(), expectedMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void updateIfTwoElemsInStorageUpdateCorrect() throws NoSuchFieldException, IllegalAccessException {
        var key1 = new MatchId(MEXICO, CANADA);
        var startTime1 = OffsetDateTime.now();
        var match1 = new Match(MEXICO, 0, CANADA, 0, startTime1);

        var key2 = new MatchId(SPAIN, BRAZIL);
        var startTime2 = OffsetDateTime.now().minusMinutes(35);
        var match2 = new Match(SPAIN, 0, BRAZIL, 0, startTime2);

        var keyForUpdate = new MatchId(SPAIN, BRAZIL);
        var matchToUpdate = new Match(SPAIN, 0, BRAZIL, 1);

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key1.getId(), match1, key2.getId(), match2));

        matchStorage.update(keyForUpdate, matchToUpdate);

        var expectedStorage = new ConcurrentHashMap<>();
        var updatedMatch = new Match(SPAIN, 0, BRAZIL, 1, startTime2);
        expectedStorage.put(key1.getId(), match1);
        expectedStorage.put(keyForUpdate.getId(), updatedMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void updateIfKeyDoesntExistThrowException() throws NoSuchFieldException, IllegalAccessException {
        var existingKey = new MatchId(MEXICO, CANADA);
        var startTime = OffsetDateTime.now();
        var existingMatch = new Match(MEXICO, 0, CANADA, 0, startTime);

        var newKey = new MatchId(SPAIN, BRAZIL);
        var newMatch = new Match(SPAIN, 0, BRAZIL, 1);

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(existingKey.getId(), existingMatch));

        assertThrows(KeyNotFoundException.class, () -> matchStorage.update(newKey, newMatch));

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(existingKey.getId(), existingMatch);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void deleteIfSingleElemInStorageSuccess() throws NoSuchFieldException, IllegalAccessException {
        var key = new MatchId(MEXICO, CANADA);
        var match = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key.getId(), match));

        matchStorage.delete(key);

        var expectedStorage = new ConcurrentHashMap<>();
        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void deleteIfTwoElemsInStorageDeleteCorrect() throws NoSuchFieldException, IllegalAccessException {
        var key1 = new MatchId(MEXICO, CANADA);
        var match1 = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var key2 = new MatchId(SPAIN, BRAZIL);
        var match2 = new Match(SPAIN, 0, BRAZIL, 0, OffsetDateTime.now().minusMinutes(35));

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key1.getId(), match1, key2.getId(), match2));

        matchStorage.delete(key2);

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(key1.getId(), match1);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void deleteIfKeyDoesntExistThrowException() throws NoSuchFieldException, IllegalAccessException {
        var key = new MatchId(MEXICO, CANADA);
        var match = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key.getId(), match));

        var keyToDelete = new MatchId(SPAIN, BRAZIL);
        assertThrows(KeyNotFoundException.class, () -> matchStorage.delete(keyToDelete));

        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(key.getId(), match);

        var actualStorage = extractStorage(matchStorage);

        assertEquals(expectedStorage, actualStorage);
    }

    @Test
    void getAllIfEmptyReturnEmptyList() {
        var matchStorage = new MatchStorage();
        var matches = matchStorage.getAll();

        assertNotNull(matches);
        assertEquals(0, matches.size());
    }

    @Test
    void getAllIfNotEmptyReturnAll() throws NoSuchFieldException, IllegalAccessException {
        var key1 = new MatchId(MEXICO, CANADA);
        var match1 = new Match(MEXICO, 0, CANADA, 0, OffsetDateTime.now());

        var key2 = new MatchId(SPAIN, BRAZIL);
        var match2 = new Match(SPAIN, 0, BRAZIL, 0, OffsetDateTime.now().minusMinutes(35));

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key1.getId(), match1, key2.getId(), match2));

        var matches = matchStorage.getAll();

        assertNotNull(matches);
        assertTrue(matches.containsAll(List.of(match1, match2)));
    }

    @Test
    void getAllIfModifyReturnedValuesStorageIsNotChanged() throws NoSuchFieldException, IllegalAccessException {
        var startTime = OffsetDateTime.now();

        var key1 = new MatchId(MEXICO, CANADA);
        var match1 = new Match(MEXICO, 0, CANADA, 0, startTime);

        var key2 = new MatchId(SPAIN, BRAZIL);
        var match2 = new Match(SPAIN, 0, BRAZIL, 0, startTime);

        var matchStorage = new MatchStorage();
        initStorage(matchStorage, Map.of(key1.getId(), match1, key2.getId(), match2));

        // we expect that nothing is changed in storage
        var expectedStorage = new ConcurrentHashMap<>();
        expectedStorage.put(new MatchId(MEXICO, CANADA).getId(),
                new Match(MEXICO, 0, CANADA, 0, startTime));
        expectedStorage.put(new MatchId(SPAIN, BRAZIL).getId(),
                new Match(SPAIN, 0, BRAZIL, 0, startTime));

        var matches = matchStorage.getAll();

        // verify that getAll didn't change anything
        var actualStorage = extractStorage(matchStorage);
        assertEquals(expectedStorage, actualStorage);

        // get one element and change something
        var m = matches.getFirst();
        m.setAwayTeamScore(1000);

        // nothing should be changed
        actualStorage = extractStorage(matchStorage);
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
