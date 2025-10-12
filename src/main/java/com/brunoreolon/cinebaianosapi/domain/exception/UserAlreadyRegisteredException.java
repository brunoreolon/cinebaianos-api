package com.brunoreolon.cinebaianosapi.domain.exception;

public class UserAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public UserAlreadyRegisteredException(String message) {
        super(message);
    }

}
