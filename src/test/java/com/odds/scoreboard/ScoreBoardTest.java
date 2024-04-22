package com.odds.scoreboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScoreBoardTest {

    private static final String INVALID_INPUT_NULL_EMPTY = "Invalid input: Params null or empty";
    private static final String INVALID_INPUT_NEGATIVE = "Invalid input: Params negative";

    private final MatchStorage matchStorage = mock(MatchStorage.class);
    private final Clock clock = Clock.fixed(Instant.parse("2024-04-22T12:00:00.00Z"), ZoneId.of("UTC"));

    @Test
    public void startMatchIfAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        sc.startMatch(homeTeam, awayTeam);

        MatchId expectedKey = new MatchId(homeTeam, awayTeam);
        OffsetDateTime expectedStartTime = OffsetDateTime.now(clock);
        Match expectedMatch = new Match(homeTeam, 0, awayTeam, 0, expectedStartTime);
        verify(matchStorage, times(1)).
                save(expectedKey, expectedMatch);
    }

    @Test
    public void startMatchIfStorageExceptionThrowException() {
        String homeTeam = "Mexico", awayTeam = "Canada";
        doThrow(RuntimeException.class).when(matchStorage)
                .save(any(), any());

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.startMatch(homeTeam, awayTeam));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void startMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(RuntimeException.class, () -> sc.startMatch(homeTeam, awayTeam));

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
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

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
    }

    @ParameterizedTest
    @CsvSource({"-1,1", "1,-1", "-1,-1"})
    public void updateScoreIfHomeAndAwayTeamScoreNegativeThrowException(int homeTeamScore, int awayTeamScore) {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(RuntimeException.class, () -> sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NEGATIVE));
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

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
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
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime nowPlus10Minutes = now.plusMinutes(10);

        List<Match> unorderedMatches = List.of(new Match("Mexico", 0, "Canada", 5, now),
                new Match("Spain", 10, "Brazil", 2, nowPlus10Minutes),
                new Match("Germany", 2, "France", 2, nowPlus10Minutes));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(new Match("Spain", 10, "Brazil", 2, nowPlus10Minutes),
                new Match("Mexico", 0, "Canada", 5, now),
                new Match("Germany", 2, "France", 2, nowPlus10Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }

    @Test
    public void matchesInProgressIfSomeTotalScoresEqualReturnOrdered() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime nowMinus10Minutes = now.minusMinutes(10);
        OffsetDateTime nowMinus20Minutes = now.minusMinutes(20);
        OffsetDateTime nowMinus30Minutes = now.minusMinutes(30);
        OffsetDateTime nowMinus40Minutes = now.minusMinutes(40);

        List<Match> unorderedMatches = List.of(
                new Match("Mexico", 0, "Canada", 5, nowMinus40Minutes),
                new Match("Spain", 10, "Brazil", 2, nowMinus30Minutes),
                new Match("Germany", 2, "France", 2, nowMinus20Minutes),
                new Match("Uruguay", 6, "Italy", 6, nowMinus10Minutes),
                new Match("Argentina", 3, "Australia", 1, now));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(
                new Match("Uruguay", 6, "Italy", 6, nowMinus10Minutes),
                new Match("Spain", 10, "Brazil", 2, nowMinus30Minutes),
                new Match("Mexico", 0, "Canada", 5, nowMinus40Minutes),
                new Match("Argentina", 3, "Australia", 1, now),
                new Match("Germany", 2, "France", 2, nowMinus20Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }

    @Test
    public void matchesInProgressIfAlreadyOrderedReturnOrdered() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime nowMinus10Minutes = now.minusMinutes(10);
        OffsetDateTime nowMinus20Minutes = now.minusMinutes(20);
        OffsetDateTime nowMinus30Minutes = now.minusMinutes(30);
        OffsetDateTime nowMinus40Minutes = now.minusMinutes(40);

        List<Match> unorderedMatches = List.of(
                new Match("Uruguay", 6, "Italy", 6, nowMinus10Minutes),
                new Match("Spain", 10, "Brazil", 2, nowMinus30Minutes),
                new Match("Mexico", 0, "Canada", 5, nowMinus40Minutes),
                new Match("Argentina", 3, "Australia", 1, now),
                new Match("Germany", 2, "France", 2, nowMinus20Minutes));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(
                new Match("Uruguay", 6, "Italy", 6, nowMinus10Minutes),
                new Match("Spain", 10, "Brazil", 2, nowMinus30Minutes),
                new Match("Mexico", 0, "Canada", 5, nowMinus40Minutes),
                new Match("Argentina", 3, "Australia", 1, now),
                new Match("Germany", 2, "France", 2, nowMinus20Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }
}
