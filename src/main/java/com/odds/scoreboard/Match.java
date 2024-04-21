package com.odds.scoreboard;

import java.util.Objects;

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

    public int hashCode() {
        return Objects.hash(homeTeamName, homeTeamScore, awayTeamName, awayTeamScore);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Match other = (Match) obj;
        return Objects.equals(homeTeamName, other.homeTeamName) &&
                Objects.equals(homeTeamScore, other.homeTeamScore) &&
                Objects.equals(awayTeamName, other.awayTeamName) &&
                Objects.equals(awayTeamScore, other.awayTeamScore);
    }

    @Override
    public String toString() {
        return "Match{" +
                "homeTeamName='" + homeTeamName + '\'' +
                ", homeTeamScore=" + homeTeamScore +
                ", awayTeamName='" + awayTeamName + '\'' +
                ", awayTeamScore=" + awayTeamScore +
                '}';
    }
}
