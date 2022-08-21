package com.github.juliherms.exceptions;

/**
 * This class responsible to customize Account not found
 */
public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
