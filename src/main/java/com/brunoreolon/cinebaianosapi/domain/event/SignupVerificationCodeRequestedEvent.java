package com.brunoreolon.cinebaianosapi.domain.event;

public record SignupVerificationCodeRequestedEvent(String email, String name, String code) {
}