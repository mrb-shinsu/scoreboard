package com.odds.scoreboard.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class MatchIdTest {
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(MatchId.class).verify();
    }
}
