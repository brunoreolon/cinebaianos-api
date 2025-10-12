package com.brunoreolon.cinebaianosapi.domain.exception;

public class MovieNotFoundException extends EntityNotFoundException {

    public MovieNotFoundException(String message) {
        super(message);
    }

}
