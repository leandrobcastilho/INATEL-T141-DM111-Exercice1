package br.com.leandro.gae_exemplo1.exception;

import java.util.logging.Logger;

public class UserNotFoundException extends Exception {

    private static final Logger log = Logger.getLogger("UserNotFoundException");

    private String message;

    public UserNotFoundException(String message) {
        super(message);
        this.message = message;
        log.info("UserNotFoundException " + message);
    }

    public String getMessage() {
        return message;
    }
}