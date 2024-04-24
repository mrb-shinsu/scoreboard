package com.odds.scoreboard.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class MatchTest {
    @Test
    void equalsContract() {
        EqualsVerifier.simple().forClass(Match.class).verify();
    }
}
