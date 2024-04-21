package com.odds.scoreboard;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ScoreBoardTest {

    private final MatchStorage matchStorage = mock(MatchStorage.class);

    @Test
    public void startMatchAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage);
        sc.startMatch(homeTeam, awayTeam);

        String expectedKey = homeTeam + "_" + awayTeam;
        Match expectedMatch = new Match(homeTeam, 0, awayTeam, 0);
        verify(matchStorage, times(1)).
                save(expectedKey, expectedMatch);
    }
}
