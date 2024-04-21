package com.odds.scoreboard;

public class Match {
    private String homeTeamName;
    private int homeTeamScore;

    private String awayTeamName;
    private int awayTeamScore;

    public Match(String homeTeamName, int homeTeamScore, String awayTeamName, int awayTeamScore) {
        this.homeTeamName = homeTeamName;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamName = awayTeamName;
        this.awayTeamScore = awayTeamScore;
    }
}
