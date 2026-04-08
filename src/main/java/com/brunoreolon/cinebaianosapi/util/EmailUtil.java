package com.brunoreolon.cinebaianosapi.util;

import com.brunoreolon.cinebaianosapi.domain.model.Email;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.Group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmailUtil {

    @Value("${frontend.url}")
    private String frontendUrl;

    public Email newUser(User user, String newPassword) {
        String subject = "Sua senha temporária - CineBaianos";
        String content = """
            <html>
              <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <p>Olá, <b>%s</b>!</p>
                <p>Sua conta no <b>CineBaianos</b> foi criada com sucesso.</p>
                <p>Sua senha temporária é:</p>
                <p><b>%s</b></p>
                <p>Por favor, clique no link abaixo para fazer o primeiro login e alterar sua senha:</p>
                <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Acessar CineBaianos</a></p>
                <hr style="border: none; border-top: 1px solid #eee;" />
                <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
              </body>
            </html>
            """.formatted(user.getName(), newPassword, frontendUrl);

        return Email.builder()
                .to(user.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email resetPasswordByAdmin(User user, String newPassword) {
        String subject = "Senha redefinida pelo administrador - Cinebaianos";
        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Olá, <b>%s</b>!</p>
                    <p>A senha da sua conta no <b>CineBaianos</b> foi redefinida por um administrador do sistema.</p>
                    <p>Sua nova senha temporária é:</p>
                    <p><b>%s</b></p>
                    <p>Por segurança, recomendamos que você faça login e altere essa senha assim que possível.</p>
                    <p>Você pode acessar a plataforma pelo link abaixo:</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Acessar CineBaianos</a></p>
                    <p>Se você não reconhece essa ação, entre em contato com o suporte.</p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(user.getName(), newPassword, frontendUrl);

        return Email.builder()
                .to(user.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email resetPasswordByRecover(User user) {
        String subject = "Senha redefinida com sucesso - CineBaianos";
        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Olá, <b>%s</b>!</p>
                    <p>Sua senha da conta no <b>CineBaianos</b> foi redefinida com sucesso.</p>
                    <p>Se você não realizou essa alteração, recomendamos que altere a senha imediatamente e entre em contato com nosso suporte.</p>
                    <p>Você pode acessar a plataforma pelo link abaixo:</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Acessar CineBaianos</a></p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(user.getName(), frontendUrl);

        return Email.builder()
                .to(user.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email recoverPassword(User user, String token) {
        String resetLink = String.format("%s/login.html?token=%s", frontendUrl, token);
        String subject = "Recuperação de senha CineBaianos";

        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Olá,</p>
                    <p>Recebemos uma solicitação para redefinir sua senha. Clique no link abaixo para criar uma nova senha:</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Redefinir Senha</a></p>
                    <p>Este link expira em 15 minutos.</p>
                    <p>Se você não solicitou, por favor, ignore este e-mail.</p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(resetLink);

        return Email.builder()
                .to(user.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email directGroupInvite(User invitedUser, User createdBy, Group group, String token, LocalDateTime expiresAt) {
        String inviteLink = String.format("%s/group-invites/accept?token=%s", frontendUrl, token);
        String expiresText = expiresAt != null
                ? expiresAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "sem expiracao";

        String subject = "Voce recebeu um convite de grupo - CineBaianos";
        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Ola, <b>%s</b>!</p>
                    <p>Voce recebeu um convite para entrar no grupo <b>%s</b> de <b>%s</b>.</p>
                    <p>Validade do convite: <b>%s</b>.</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Aceitar Convite</a></p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(invitedUser.getName(), group.getName(), createdBy.getName(), expiresText, inviteLink);

        return Email.builder()
                .to(invitedUser.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email groupInviteAcceptedForCreator(User creator, User acceptedBy, Group group) {
        String subject = "Seu convite foi aceito - CineBaianos";
        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Ola, <b>%s</b>!</p>
                    <p>O usuario <b>%s</b> aceitou o convite e entrou no grupo <b>%s</b>.</p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(creator.getName(), acceptedBy.getName(), group.getName());

        return Email.builder()
                .to(creator.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email groupInviteAcceptedForMember(User acceptedBy, Group group) {
        String subject = "Convite aceito com sucesso - CineBaianos";
        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Ola, <b>%s</b>!</p>
                    <p>Seu convite foi aceito e voce agora faz parte do grupo <b>%s</b>.</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Acessar Plataforma</a></p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(acceptedBy.getName(), group.getName(), frontendUrl);

        return Email.builder()
                .to(acceptedBy.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email groupJoinRequestCreated(User manager, User requester, Group group) {
        String subject = "Nova solicitacao para entrar no grupo - CineBaianos";
        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Ola, <b>%s</b>!</p>
                    <p>O usuario <b>%s</b> solicitou entrada no grupo <b>%s</b>.</p>
                    <p>Acesse a area de membros para aprovar ou rejeitar a solicitacao.</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Gerenciar Solicitacoes</a></p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(manager.getName(), requester.getName(), group.getName(), frontendUrl);

        return Email.builder()
                .to(manager.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

    public Email groupJoinRequestReviewed(User requester, Group group, boolean approved) {
        String subject = approved
                ? "Sua solicitacao foi aprovada - CineBaianos"
                : "Sua solicitacao foi rejeitada - CineBaianos";

        String statusText = approved
                ? "foi aprovada"
                : "foi rejeitada";

        String content = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Ola, <b>%s</b>!</p>
                    <p>Sua solicitacao para entrar no grupo <b>%s</b> %s.</p>
                    <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Acessar Plataforma</a></p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #999;">CineBaianos &copy; 2025</p>
                  </body>
                </html>
                """.formatted(requester.getName(), group.getName(), statusText, frontendUrl);

        return Email.builder()
                .to(requester.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }

}