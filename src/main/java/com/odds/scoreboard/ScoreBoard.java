package com.odds.scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreBoard {

    private MatchStorage storage;

    public ScoreBoard(MatchStorage storage) {
        this.storage = storage;
    }

    public void startMatch(String homeTeam, String awayTeam) {
        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            throw new RuntimeException("Invalid input: Home/away team null or empty");
        }

        Match match = new Match(homeTeam, 0, awayTeam, 0);
        String matchId = homeTeam + "_" + awayTeam;

        storage.save(matchId, match);
    }

    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            throw new RuntimeException("Invalid input: Home/away team null or empty");
        }
        if (homeTeamScore < 0 || awayTeamScore < 0) {
            throw new RuntimeException("Invalid input: Home/away team score negative");
        }

        Match match = new Match(homeTeam, homeTeamScore, awayTeam, awayTeamScore);
        String matchId = homeTeam + "_" + awayTeam;

        storage.update(matchId, match);
    }

    public void finishMatch(String homeTeam, String awayTeam) {
        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            throw new RuntimeException("Invalid input: Home/away team null or empty");
        }

        String matchId = homeTeam + "_" + awayTeam;

        storage.delete(matchId);
    }

    public List<Match> matchesInProgress() {
        List<Match> matches = storage.getAll();
        Comparator<Match> cmp = Comparator.comparing(Match::getTotalScore).reversed();

        return matches.stream()
                .sorted(cmp)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
