package com.brunoreolon.cinebaianosapi.domain.exception;

public class MovieAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public MovieAlreadyRegisteredException(String message) {
        super(message);
    }

}
