package com.odds.scoreboard.domain;

import com.odds.scoreboard.BaseTest;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchIdTest extends BaseTest {
    @Test
    void equalsContract() {
        EqualsVerifier.simple().forClass(MatchId.class).verify();
    }

    @Test
    void getIdCheckConstructor() {
        var matchId = new MatchId(MEXICO, CANADA);

        assertEquals(MEXICO + "_" + CANADA, matchId.getId());
    }
}
