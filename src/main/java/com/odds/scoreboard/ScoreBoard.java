package com.odds.scoreboard;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreBoard {

    private final MatchStorage storage;
    private final Clock clock;

    public ScoreBoard(MatchStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    public void startMatch(String homeTeam, String awayTeam) {
        validateNotNullOrEmpty(homeTeam, awayTeam);

        OffsetDateTime startTime = OffsetDateTime.now(clock);
        Match match = new Match(homeTeam, 0, awayTeam, 0, startTime);
        MatchId matchId = new MatchId(homeTeam, awayTeam);

        storage.save(matchId, match);
    }

    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
        validateNotNullOrEmpty(homeTeam, awayTeam);
        validateNotNegative(homeTeamScore, awayTeamScore);

        Match match = new Match(homeTeam, homeTeamScore, awayTeam, awayTeamScore);
        MatchId matchId = new MatchId(homeTeam, awayTeam);

        storage.update(matchId, match);
    }

    public void finishMatch(String homeTeam, String awayTeam) {
        validateNotNullOrEmpty(homeTeam, awayTeam);

        String matchId = homeTeam + "_" + awayTeam;

        storage.delete(matchId);
    }

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
                throw new RuntimeException("Invalid input: Params null or empty");
            }
        }
    }

    private void validateNotNegative(int... params) {
        for (int p : params) {
            if (p < 0) {
                throw new RuntimeException("Invalid input: Params negative");
            }
        }
    }
}
