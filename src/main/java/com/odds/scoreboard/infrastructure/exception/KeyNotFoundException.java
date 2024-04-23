package com.odds.scoreboard.infrastructure.exception;

public class KeyNotFoundException extends RuntimeException {
    public KeyNotFoundException() {
        super("Key not found");
    }
}
