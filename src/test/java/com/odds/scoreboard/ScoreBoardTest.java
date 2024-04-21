package com.odds.scoreboard;

import org.junit.jupiter.api.Test;

public class ScoreBoardTest {

    @Test
    public void startMatchAllValidSuccess() {
        String homeTeam = "Mexico", awayTeam = "Canada";

        ScoreBoard sc = new ScoreBoard();
        sc.startMatch(homeTeam, awayTeam);
    }
}
