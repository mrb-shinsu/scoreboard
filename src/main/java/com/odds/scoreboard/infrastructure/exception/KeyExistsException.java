package com.odds.scoreboard.infrastructure.exception;

public class KeyExistsException extends RuntimeException {
    public KeyExistsException() {
        super("Key already exists");
    }
}
