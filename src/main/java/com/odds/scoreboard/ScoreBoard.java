package com.odds.scoreboard;

public class ScoreBoard {

    private MatchStorage storage;

    public ScoreBoard(MatchStorage storage) {
        this.storage = storage;
    }

    public void startMatch(String homeTeam, String awayTeam) {
        Match match = new Match(homeTeam, 0, awayTeam, 0);
        String matchId = homeTeam + "_" + awayTeam;

        storage.save(matchId, match);
    }
}
