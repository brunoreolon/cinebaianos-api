package com.brunoreolon.cinebaianosapi.domain.listenner;

import com.brunoreolon.cinebaianosapi.domain.event.PasswordRecoveredEvent;
import com.brunoreolon.cinebaianosapi.domain.event.PasswordResetByAdminEvent;
import com.brunoreolon.cinebaianosapi.domain.event.PasswordResetByRecoverEvent;
import com.brunoreolon.cinebaianosapi.domain.event.UserCreatedEvent;
import com.brunoreolon.cinebaianosapi.domain.model.Email;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.service.NotificationService;
import com.brunoreolon.cinebaianosapi.util.EmailUtil;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailListener {

    private final NotificationService notificationService;
    private final EmailUtil emailUtil;

    public EmailListener(NotificationService notificationService, EmailUtil emailUtil) {
        this.notificationService = notificationService;
        this.emailUtil = emailUtil;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void usuarioCriadoListener(UserCreatedEvent event) {
        User user = event.user();
        Email email = emailUtil.newUser(user, event.password());

        notificationService.send(email);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void recuperacaoSenhaListener(PasswordRecoveredEvent event) {
        User user = event.user();
        Email email = emailUtil.recoverPassword(user, event.token());

        notificationService.send(email);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void redefinicaoSenhaByRecuperacaoListener(PasswordResetByRecoverEvent event) {
        User user = event.user();
        Email email = emailUtil.resetPasswordByRecover(user);

        notificationService.send(email);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void redefinicaoSenhaByAdminListener(PasswordResetByAdminEvent event) {
        User user = event.user();
        Email email = emailUtil.resetPasswordByRecover(user);

        notificationService.send(email);
    }

}
