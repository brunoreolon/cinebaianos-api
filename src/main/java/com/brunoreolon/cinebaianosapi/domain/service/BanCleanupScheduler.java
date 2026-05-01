package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.repository.GroupMemberBanRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BanCleanupScheduler {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberBanRepository groupMemberBanRepository;

    @Transactional
    @Scheduled(cron = "${jobs.ban-cleanup.cron:0 0 */2 * * *}")
    public void cleanupExpiredBans() {
        LocalDateTime now = LocalDateTime.now();

        int userBansCleared = userRepository.clearExpiredBans(now);
        int groupBansCleared = groupRepository.clearExpiredBans(now);
        int groupMemberBansDeleted = groupMemberBanRepository.deleteExpiredBans(now);

        if (userBansCleared > 0 || groupBansCleared > 0 || groupMemberBansDeleted > 0) {
            log.info("Ban cleanup executed. users={}, groups={}, groupMemberBans={}",
                    userBansCleared, groupBansCleared, groupMemberBansDeleted);
        }
    }

}