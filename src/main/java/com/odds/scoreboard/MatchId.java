package com.odds.scoreboard;

import java.util.Objects;

public class MatchId {
    private String id;

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
    public String toString() {
        return "MatchId{" +
                "id='" + id + '\'' +
                '}';
    }
}
