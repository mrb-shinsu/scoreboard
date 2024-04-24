package com.odds.scoreboard.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class MatchIdTest {
    @Test
    void equalsContract() {
        EqualsVerifier.simple().forClass(MatchId.class).verify();
    }
}
