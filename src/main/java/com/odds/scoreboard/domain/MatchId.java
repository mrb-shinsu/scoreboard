package com.odds.scoreboard.domain;

import java.util.Objects;

/**
 * Identifier for matches between two teams.
 * It's constructed as homeTeamName + "_" + awayTeamName
 */
public class MatchId {
    private final String id;

    public MatchId(String homeTeam, String awayTeam) {
        this.id = homeTeam + "_" + awayTeam;
    }

    public String getId() {
        return this.id;
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

        MatchId other = (MatchId) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MatchId{" +
                "id='" + id + '\'' +
                '}';
    }
}
