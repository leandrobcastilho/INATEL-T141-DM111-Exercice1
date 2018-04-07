package br.com.leandro.gae_exemplo1.exception;

import java.util.logging.Logger;

public class UserAlreadyExistsException extends Exception {

    private static final Logger log = Logger.getLogger("UserNotFoundException");

    private String message;

    public UserAlreadyExistsException(String message) {
        super(message);
        this.message = message;

        log.info("UserAlreadyExistsException " + message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}