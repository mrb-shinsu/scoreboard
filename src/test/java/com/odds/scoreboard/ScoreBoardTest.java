package com.odds.scoreboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ScoreBoardTest {

    private final MatchStorage matchStorage = mock(MatchStorage.class);

    @Test
    public void startMatchIfAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage);
        sc.startMatch(homeTeam, awayTeam);

        String expectedKey = homeTeam + "_" + awayTeam;
        Match expectedMatch = new Match(homeTeam, 0, awayTeam, 0);
        verify(matchStorage, times(1)).
                save(expectedKey, expectedMatch);
    }

    @Test
    public void startMatchIfStorageExceptionThrowException() {
        String homeTeam = "Mexico", awayTeam = "Canada";
        doThrow(RuntimeException.class).when(matchStorage)
                .save(anyString(), any());

        ScoreBoard sc = new ScoreBoard(matchStorage);
        assertThrows(RuntimeException.class, () -> sc.startMatch(homeTeam, awayTeam));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void startMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        ScoreBoard sc = new ScoreBoard(matchStorage);
        Exception e = assertThrows(RuntimeException.class, () -> sc.startMatch(homeTeam, awayTeam));

        String expectedMessage = "Invalid input: Home/away team null or empty";
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void updateScoreIfAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";
        int homeTeamScore = 1, awayTeamScore = 2;

        ScoreBoard sc = new ScoreBoard(matchStorage);
        sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore);

        String expectedKey = homeTeam + "_" + awayTeam;
        Match expectedMatch = new Match(homeTeam, homeTeamScore, awayTeam, awayTeamScore);
        verify(matchStorage, times(1)).
                update(expectedKey, expectedMatch);
    }

    @Test
    public void updateScoreIfStorageExceptionThrowException() {
        String homeTeam = "Mexico", awayTeam = "Canada";
        int homeTeamScore = 1, awayTeamScore = 2;
        doThrow(RuntimeException.class).when(matchStorage)
                .update(anyString(), any());

        ScoreBoard sc = new ScoreBoard(matchStorage);
        assertThrows(RuntimeException.class, () -> sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));
    }

}
