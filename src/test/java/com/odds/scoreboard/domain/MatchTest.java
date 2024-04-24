package com.odds.scoreboard.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class MatchTest {
    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(Match.class).verify();
    }
}
