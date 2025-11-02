package com.brunoreolon.cinebaianosapi.core;

import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.VoteTypeRepository;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRegistratioService userRegistratioService;
    private final UserRepository userRepository;
    private final VoteTypeRepository voteTypeRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRegistratioService.create(User.builder()
                    .discordId("339251538998329354")
                    .name("Bruno Reolon")
                    .email("bruno.reolonn@gmail.com")
                    .password("bruno1342")
                    .build());

            userRegistratioService.create(User.builder()
                    .discordId("555470950892568576")
                    .name("AʟᴍɪʀVXɢᴀᴍᴇʀ")
                    .email("bruno.reolonn+user2@gmail.com")
                    .password("almir1342")
                    .build());

            userRegistratioService.create(User.builder()
                    .discordId("405389229762281473")
                    .name("FabioParadyze")
                    .email("bruno.reolonn+user3@gmail.com")
                    .password("fabio1342")
                    .build());

            userRegistratioService.create(User.builder()
                    .discordId("271749848842108928")
                    .name("Ro00dr1go")
                    .email("bruno.reolonn+user4@gmail.com")
                    .password("rodrigo1342")
                    .build());

            voteTypeRepository.save(VoteType.builder()
                    .name("DA_HORA")
                    .description("Da Hora")
                    .active(true)
                    .build());

            voteTypeRepository.save(VoteType.builder()
                    .name("LIXO")
                    .description("Lixo")
                    .active(true)
                    .build());

            voteTypeRepository.save(VoteType.builder()
                    .name("NAO_ASSISTI")
                    .description("Não Assisti")
                    .active(true)
                    .build());
        }
    }
}