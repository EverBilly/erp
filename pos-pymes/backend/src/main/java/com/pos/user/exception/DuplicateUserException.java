package com.pos.user.exception;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String field, String value) {
        super("Ya existe un usuario con " + field + ": " + value);
    }
}
