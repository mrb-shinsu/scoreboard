package com.odds.scoreboard.domain;

import com.odds.scoreboard.BaseTest;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchTest extends BaseTest {
    @Test
    void equalsContract() {
        EqualsVerifier.simple().forClass(Match.class).verify();
    }

    @Test
    void getTotalScoreIfZeroReturnZero() {
        var match = new Match(MEXICO, 0, CANADA, 0);

        assertEquals(0, match.getTotalScore());
    }

    @Test
    void getTotalScoreIfGtZeroReturnTotal() {
        var match = new Match(MEXICO, 1, CANADA, 7);

        assertEquals(8, match.getTotalScore());
    }
}
