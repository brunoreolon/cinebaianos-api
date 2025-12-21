package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.event.UserCreatedEvent;
import com.brunoreolon.cinebaianosapi.domain.exception.EntityInUseException;
import com.brunoreolon.cinebaianosapi.domain.exception.UserAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.UserNotFoundException;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRegistratioService implements OwnableService<User, String> {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public User create(User user) {
        boolean existsById = userRepository.existsById(user.getDiscordId());

        if (existsById)
            throw new UserAlreadyRegisteredException(String.format("There is already a user registered with the discord id '%s'",
                    user.getDiscordId()));

        String newPassword = generateRandomPassword(8);

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.activate();
        User newUser = userRepository.save(user);

        publisher.publishEvent(new UserCreatedEvent(newUser, newPassword));

        return newUser;
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@";
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
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

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void delete(String discordId) {
        User user = get(discordId);

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new EntityInUseException(String.format("Cannot delete user with id '%s' because it has associated movies.", discordId));
        }
    }

    @Override
    public User get(String discordId) {
        return userRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with discordId '%s' not found", discordId)));
    }

}
