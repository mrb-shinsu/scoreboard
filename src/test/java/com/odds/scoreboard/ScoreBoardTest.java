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
    void startMatchIfAllValidSuccess() {
        var scoreBoard = new ScoreBoard(matchStorage, clock);
        scoreBoard.startMatch(MEXICO, CANADA);

        var expectedKey = new MatchId(MEXICO, CANADA);
        var expectedStartTime = OffsetDateTime.now(clock);
        var expectedMatch = new Match(MEXICO, 0, CANADA, 0, expectedStartTime);

        verify(matchStorage, times(1)).
                save(expectedKey, expectedMatch);
    }

    @Test
    void startMatchIfStorageExceptionThrowException() {
        doThrow(RuntimeException.class).when(matchStorage)
                .save(any(), any());

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class,
                () -> scoreBoard.startMatch(MEXICO, CANADA));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    void startMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var e = assertThrows(IllegalArgumentException.class,
                () -> scoreBoard.startMatch(homeTeam, awayTeam));

        var actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
    }

    @Test
    void updateScoreIfAllValidSuccess() {
        int homeTeamScore = 1, awayTeamScore = 2;

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        scoreBoard.updateScore(MEXICO, homeTeamScore, CANADA, awayTeamScore);

        var expectedKey = new MatchId(MEXICO, CANADA);
        var expectedMatch = new Match(MEXICO, homeTeamScore, CANADA, awayTeamScore);

        verify(matchStorage, times(1)).
                update(expectedKey, expectedMatch);
    }

    @Test
    void updateScoreIfStorageExceptionThrowException() {
        int homeTeamScore = 1, awayTeamScore = 2;
        doThrow(RuntimeException.class).when(matchStorage)
                .update(any(), any());

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class,
                () -> scoreBoard.updateScore(MEXICO, homeTeamScore, CANADA, awayTeamScore));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    void updateScoreIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        int homeTeamScore = 1, awayTeamScore = 2;

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var e = assertThrows(IllegalArgumentException.class,
                () -> scoreBoard.updateScore(homeTeam, homeTeamScore, awayTeam, awayTeamScore));

        var actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
    }

    @ParameterizedTest
    @CsvSource({"-1,1", "1,-1", "-1,-1"})
    void updateScoreIfHomeAndAwayTeamScoreNegativeThrowException(int homeTeamScore, int awayTeamScore) {
        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var e = assertThrows(IllegalArgumentException.class,
                () -> scoreBoard.updateScore(MEXICO, homeTeamScore, CANADA, awayTeamScore));

        var actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NEGATIVE));
    }

    @Test
    void finishMatchIfAllValidSuccess() {
        var scoreBoard = new ScoreBoard(matchStorage, clock);
        scoreBoard.finishMatch(MEXICO, CANADA);

        var expectedKey = new MatchId(MEXICO, CANADA);

        verify(matchStorage, times(1)).
                delete(expectedKey);
    }

    @Test
    void finishMatchIfStorageExceptionThrowException() {
        doThrow(RuntimeException.class).when(matchStorage)
                .delete(any());

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        assertThrows(RuntimeException.class, () -> scoreBoard.finishMatch(MEXICO, CANADA));
    }

    @ParameterizedTest
    @CsvSource({",Canada", "'',Canada", "Mexico,", "Mexico,''", ",", ",''", "'',", "'',''"})
    void finishMatchIfHomeAndAwayTeamNullOrEmptyThrowException(String homeTeam, String awayTeam) {
        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var e = assertThrows(IllegalArgumentException.class,
                () -> scoreBoard.finishMatch(homeTeam, awayTeam));

        var actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(INVALID_INPUT_NULL_EMPTY));
    }

    @Test
    void matchesInProgressIfNoMatchesReturnEmptyList() {
        when(matchStorage.getAll()).thenReturn(List.of());

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var matches = scoreBoard.matchesInProgress();

        assertNotNull(matches);
        assertEquals(0, matches.size());
    }

    @Test
    void matchesInProgressIfOneReturnSingleElemList() {
        var expectedMatches = List.of(new Match(MEXICO, 0, CANADA, 0));
        when(matchStorage.getAll()).thenReturn(expectedMatches);

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var matches = scoreBoard.matchesInProgress();

        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }

    @Test
    void matchesInProgressIfDiffTotalScoresReturnOrdered() {
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var nowPlus10Minutes = now.plusMinutes(10);

        var unorderedMatches = List.of(
                new Match(MEXICO, 0, CANADA, 5, now),
                new Match(SPAIN, 10, BRAZIL, 2, nowPlus10Minutes),
                new Match(GERMANY, 2, FRANCE, 2, nowPlus10Minutes));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var matches = scoreBoard.matchesInProgress();

        var expectedMatches = List.of(
                new Match(SPAIN, 10, BRAZIL, 2, nowPlus10Minutes),
                new Match(MEXICO, 0, CANADA, 5, now),
                new Match(GERMANY, 2, FRANCE, 2, nowPlus10Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }

    @Test
    void matchesInProgressIfSomeTotalScoresEqualReturnOrdered() {
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var nowMinus10Minutes = now.minusMinutes(10);
        var nowMinus20Minutes = now.minusMinutes(20);
        var nowMinus30Minutes = now.minusMinutes(30);
        var nowMinus40Minutes = now.minusMinutes(40);

        var unorderedMatches = List.of(
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes),
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var matches = scoreBoard.matchesInProgress();

        var expectedMatches = List.of(
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }

    @Test
    void matchesInProgressIfAlreadyOrderedReturnOrdered() {
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var nowMinus10Minutes = now.minusMinutes(10);
        var nowMinus20Minutes = now.minusMinutes(20);
        var nowMinus30Minutes = now.minusMinutes(30);
        var nowMinus40Minutes = now.minusMinutes(40);

        var unorderedMatches = List.of(
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes));
        when(matchStorage.getAll()).thenReturn(unorderedMatches);

        var scoreBoard = new ScoreBoard(matchStorage, clock);
        var matches = scoreBoard.matchesInProgress();

        var expectedMatches = List.of(
                new Match(URUGUAY, 6, ITALY, 6, nowMinus10Minutes),
                new Match(SPAIN, 10, BRAZIL, 2, nowMinus30Minutes),
                new Match(MEXICO, 0, CANADA, 5, nowMinus40Minutes),
                new Match(ARGENTINA, 3, AUSTRALIA, 1, now),
                new Match(GERMANY, 2, FRANCE, 2, nowMinus20Minutes));
        assertNotNull(matches);
        assertEquals(expectedMatches, matches);
    }
}
