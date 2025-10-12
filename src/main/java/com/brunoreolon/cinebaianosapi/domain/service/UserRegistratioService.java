package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.exception.UserAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.UserNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserRegistratioService {

    private final UserRepository userRepository;

    @Transactional
    public User create(User user) {
        boolean existsById = userRepository.existsById(user.getDiscordId());

        if (existsById)
            throw new UserAlreadyRegisteredException(String.format("There is already a user registered with the discord id '%s'",
                    user.getDiscordId()));

        //TODO enviar email com a senha

        return userRepository.save(user);
    }

    public User update(User user) {
        validateEmail(user);
        return performUpdate(user);
    }

    private void validateEmail(User user) {
        boolean emailAlreadyExists = userRepository.findByEmail(user.getEmail())
                .filter(v -> !v.equals(user))
                .isPresent();

        if (emailAlreadyExists)
            throw new UserAlreadyRegisteredException(String.format("There is already a user registered with the email '%s'",
                    user.getEmail()));
    }

    @Transactional
    public User performUpdate(User user) {
        return userRepository.save(user);
    }

    public User get(String discordId) {
        return userRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with discordId '%s' not found", discordId)));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void delete(String discordId) {
        User user = get(discordId);
        userRepository.delete(user);
    }

}
