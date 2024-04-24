package com.odds.scoreboard;

import com.odds.scoreboard.domain.Match;
import com.odds.scoreboard.domain.MatchId;
import com.odds.scoreboard.infrastructure.MatchStorage;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ScoreBoard for keeping track about all ongoing matches and their scores.
 * All operations are thread safe, but don't entail any locking/synchronization.
 * If certain order of operations performed by different threads is expected, external synchronization must be used
 */
public class ScoreBoard {
    private final MatchStorage storage;
    private final Clock clock;

    public ScoreBoard(MatchStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    /**
     * Start new match between {@code homeTeam} and {@code awayTeam}, with initial score 0-0
     * and save it to board.
     * <p>
     * If match is already started, {@code KeyExistsException} is thrown.
     *
     * @param homeTeam home team name, not null, not empty
     * @param awayTeam away team name, not null, not empty
     */
    public void startMatch(String homeTeam, String awayTeam) {
        validateNotNullOrEmpty(homeTeam, awayTeam);

        OffsetDateTime startTime = OffsetDateTime.now(clock);
        Match match = new Match(homeTeam, 0, awayTeam, 0, startTime);
        MatchId matchId = new MatchId(homeTeam, awayTeam);

        storage.save(matchId, match);
    }

    /**
     * Update score for match between {@code homeTeam} and {@code awayTeam}.
     * <p>
     * If match is not on board, {@code KeyNotFoundException} is thrown.
     *
     * @param homeTeam      home team name, not null, not empty
     * @param homeTeamScore home team score, greater then 0
     * @param awayTeam      away team name, not null, not empty
     * @param awayTeamScore away team score, greater then 0
     */
    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
        validateNotNullOrEmpty(homeTeam, awayTeam);
        validateNotNegative(homeTeamScore, awayTeamScore);

        Match match = new Match(homeTeam, homeTeamScore, awayTeam, awayTeamScore);
        MatchId matchId = new MatchId(homeTeam, awayTeam);

        storage.update(matchId, match);
    }

    /**
     * Finish match between {@code homeTeam} and {@code awayTeam} and remove it from board.
     * <p>
     * If match is not on board, {@code KeyNotFoundException} is thrown.
     *
     * @param homeTeam home team name, not null, not empty
     * @param awayTeam away team name, not null, not empty
     */
    public void finishMatch(String homeTeam, String awayTeam) {
        validateNotNullOrEmpty(homeTeam, awayTeam);

        MatchId matchId = new MatchId(homeTeam, awayTeam);

        storage.delete(matchId);
    }

    /**
     * Get list of matches in progress.
     * Matches are ordered by their total score descending. If they have same total score, ordering will be by the
     * most recently started match.
     * <p>
     * Returned list is mutable, but is copy of matches from the board, so changes on that list cannot affect the board.
     *
     * @return ordered list of matches
     */
    public List<Match> matchesInProgress() {
        List<Match> matches = storage.getAll();
        Comparator<Match> cmp = Comparator.comparing(Match::getTotalScore)
                .thenComparing(Match::getStartTime)
                .reversed();

        return matches.stream()
                .sorted(cmp)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void validateNotNullOrEmpty(String... params) {
        for (String p : params) {
            if (p == null || p.isBlank()) {
                throw new IllegalArgumentException("Invalid input: Params null or empty");
            }
        }
    }

    private void validateNotNegative(int... params) {
        for (int p : params) {
            if (p < 0) {
                throw new IllegalArgumentException("Invalid input: Params negative");
            }
        }
    }
}
