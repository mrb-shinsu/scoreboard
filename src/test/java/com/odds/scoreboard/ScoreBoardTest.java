package com.odds.scoreboard;

import com.odds.scoreboard.domain.Match;
import com.odds.scoreboard.domain.MatchId;
import com.odds.scoreboard.infrastructure.MatchStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ScoreBoardTest extends BaseTest {
    private static final String INVALID_INPUT_NULL_EMPTY = "Invalid input: Params null or empty";
    private static final String INVALID_INPUT_NEGATIVE = "Invalid input: Params negative";

    private final MatchStorage matchStorage = mock(MatchStorage.class);
    private final Clock clock = Clock.fixed(Instant.parse("2024-04-22T12:00:00.00Z"), ZoneId.of("UTC"));

    @Test
    public void startMatchIfAllValidSuccess() {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        sc.startMatch(MEXICO, CANADA);

        MatchId expectedKey = new MatchId(MEXICO, CANADA);
        OffsetDateTime expectedStartTime = OffsetDateTime.now(clock);
        Match expectedMatch = new Match(MEXICO, 0, CANADA, 0, expectedStartTime);
        verify(matchStorage, times(1)).
                save(expectedKey, expectedMatch);
    }

    @Test
    public void startMatchIfStorageExceptionThrowException() {
        doThrow(RuntimeException.class).when(matchStorage)
                .save(any(), any());

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.startMatch(MEXICO, CANADA));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void startMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(IllegalArgumentException.class, () -> sc.startMatch(homeTeam, awayTeam));

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
    }

    @Test
    public void updateScoreIfAllValidSuccess() {
        int homeTeamScore = 1, awayTeamScore = 2;

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        sc.updateScore(MEXICO, homeTeamScore, CANADA, awayTeamScore);

        MatchId expectedKey = new MatchId(MEXICO, CANADA);
        Match expectedMatch = new Match(MEXICO, homeTeamScore, CANADA, awayTeamScore);
        verify(matchStorage, times(1)).
                update(expectedKey, expectedMatch);
    }

    @Test
    public void updateScoreIfStorageExceptionThrowException() {
        int homeTeamScore = 1, awayTeamScore = 2;
        doThrow(RuntimeException.class).when(matchStorage)
                .update(any(), any());

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.updateScore(MEXICO, homeTeamScore, CANADA, awayTeamScore));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void updateScoreIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        int homeTeamScore = 1, awayTeamScore = 2;

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(IllegalArgumentException.class, () -> sc.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
    }

    @ParameterizedTest
    @CsvSource({"-1,1", "1,-1", "-1,-1"})
    public void updateScoreIfHomeAndAwayTeamScoreNegativeThrowException(int homeTeamScore, int awayTeamScore) {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(IllegalArgumentException.class, () -> sc.updateScore(MEXICO, homeTeamScore, CANADA, awayTeamScore));

        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NEGATIVE));
    }

    @Test
    public void finishMatchIfAllValidSuccess() {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        sc.finishMatch(MEXICO, CANADA);

        MatchId expectedKey = new MatchId(MEXICO, CANADA);
        verify(matchStorage, times(1)).
                delete(expectedKey);
    }

    @Test
    public void finishMatchIfStorageExceptionThrowException() {
        doThrow(RuntimeException.class).when(matchStorage)
                .delete(any());

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> sc.finishMatch(MEXICO, CANADA));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    public void finishMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        Exception e = assertThrows(IllegalArgumentException.class, () -> sc.finishMatch(homeTeam, awayTeam));

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
        List<Match> expectedMatches = List.of(new Match(MEXICO, 0, CANADA, 0));
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

        List<Match> unorderedMatches = List.of(new Match(MEXICO, 0, CANADA, 5, now),
                new Match(SPAIN, 10, BRAZIL, 2, nowPlus10Minutes),
                new Match(GERMANY, 2, FRANCE, 2, nowPlus10Minutes));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(new Match(SPAIN, 10, BRAZIL, 2, nowPlus10Minutes),
                new Match(MEXICO, 0, CANADA, 5, now),
                new Match(GERMANY, 2, FRANCE, 2, nowPlus10Minutes));
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
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes),
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes));
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
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        ScoreBoard sc = new ScoreBoard(matchStorage, clock);
        List<Match> matches = sc.matchesInProgress();

        List<Match> expectedMatches = List.of(
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }
}
