package com.odds.scoreboard.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Match {
    private final String homeTeamName;
    private int homeTeamScore;

    private final String awayTeamName;
    private int awayTeamScore;

    private OffsetDateTime startTime;

    public Match(String homeTeamName, int homeTeamScore, String awayTeamName, int awayTeamScore) {
        this.homeTeamName = homeTeamName;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamName = awayTeamName;
        this.awayTeamScore = awayTeamScore;
    }

    public Match(String homeTeamName, int homeTeamScore, String awayTeamName, int awayTeamScore, OffsetDateTime startTime) {
        this(homeTeamName, homeTeamScore, awayTeamName, awayTeamScore);
        this.startTime = startTime;
    }

    public Match(Match other) {
        this(other.homeTeamName, other.homeTeamScore, other.awayTeamName, other.awayTeamScore);
        this.startTime = other.startTime;
    }

    public int getTotalScore() {
        return this.homeTeamScore + this.awayTeamScore;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public int getHomeTeamScore() {
        return homeTeamScore;
    }

    public void setHomeTeamScore(int homeTeamScore) {
        this.homeTeamScore = homeTeamScore;
    }

    public int getAwayTeamScore() {
        return awayTeamScore;
    }

    public void setAwayTeamScore(int awayTeamScore) {
        this.awayTeamScore = awayTeamScore;
    }

    public int hashCode() {
        return Objects.hash(homeTeamName, homeTeamScore, awayTeamName, awayTeamScore, startTime);
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
                Objects.equals(awayTeamScore, other.awayTeamScore) &&
                Objects.equals(startTime, other.startTime);
    }

    @Override
    public String toString() {
        return "Match{" +
                "homeTeamName='" + homeTeamName + '\'' +
                ", homeTeamScore=" + homeTeamScore +
                ", awayTeamName='" + awayTeamName + '\'' +
                ", awayTeamScore=" + awayTeamScore +
                ", startTime=" + startTime +
                '}';
    }
}
