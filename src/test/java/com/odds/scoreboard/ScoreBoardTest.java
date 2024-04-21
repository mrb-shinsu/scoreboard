package com.odds.scoreboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScoreBoardTest {

    private final MatchStorage matchStorage = mock(MatchStorage.class);
    private final Clock clock = Clock.fixed(Instant.parse("2024-04-22T12:00:00.00Z"), ZoneId.of("UTC"));

    @Test
    public void startMatchIfAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
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

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.startMatch(homeTeam, awayTeam));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void startMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(RuntimeException.class, () -> sc.startMatch(homeTeam, awayTeam));

        String expectedMessage = "Invalid input: Home/away team null or empty";
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void updateScoreIfAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";
        int homeTeamScore = 1, awayTeamScore = 2;

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
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

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void updateScoreIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        int homeTeamScore = 1, awayTeamScore = 2;

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(RuntimeException.class, () -> sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));

        String expectedMessage = "Invalid input: Home/away team null or empty";
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource({"-1,1", "1,-1", "-1,-1"})
    public void updateScoreIfHomeAndAwayTeamScoreNegativeThrowException(int homeTeamScore, int awayTeamScore) {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(RuntimeException.class, () -> sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));

        String expectedMessage = "Invalid input: Home/away team score negative";
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void finishMatchIfAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        sc.finishMatch(homeTeam, awayTeam);

        String expectedKey = homeTeam + "_" + awayTeam;
        verify(matchStorage, times(1)).
                delete(expectedKey);
    }

    @Test
    public void finishMatchIfStorageExceptionThrowException() {
        String homeTeam = "Mexico", awayTeam = "Canada";
        doThrow(RuntimeException.class).when(matchStorage)
                .delete(anyString());

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.finishMatch(homeTeam, awayTeam));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void finishMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(RuntimeException.class, () -> sc.finishMatch(homeTeam, awayTeam));

        String expectedMessage = "Invalid input: Home/away team null or empty";
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void matchesInProgressIfNoMatchesReturnEmptyList() {
        when(matchStorage.getAll()).thenReturn(List.of());

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        assertNotNull(matches);
        assertEquals(0, matches.size());
    }

    @Test
    public void matchesInProgressIfOneReturnSingleElemList() {
        List<Match> expectedMatches = List.of(new Match("Mexico", 0, "Canada", 0));
        when(matchStorage.getAll()).thenReturn(expectedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }

    @Test
    public void matchesInProgressIfDiffTotalScoresReturnOrdered() {
        List<Match> unorderedMatches = List.of(new Match("Mexico", 0, "Canada", 5),
                new Match("Spain", 10, "Brazil", 2),
                new Match("Germany", 2, "France", 2));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(new Match("Spain", 10, "Brazil", 2),
                new Match("Mexico", 0, "Canada", 5),
                new Match("Germany", 2, "France", 2));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }
}
