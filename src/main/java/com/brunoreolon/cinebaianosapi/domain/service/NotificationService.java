package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.model.Email;

public interface NotificationService {

    void send(Email email);

}
