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

    public Email signupVerificationCode(String email, String name, String code) {
        String subject = "Confirme seu cadastro - CineBaianos";
        String content = """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <style>
                      body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif; line-height: 1.6; color: #333; }
                      .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                      .header { text-align: center; margin-bottom: 30px; }
                      .code-container { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0; }
                      .code { font-family: 'Courier New', 'Courier', monospace; font-size: 42px; font-weight: bold; letter-spacing: 8px; color: white; word-break: break-all; }
                      .code-label { font-size: 14px; color: rgba(255, 255, 255, 0.8); margin-bottom: 15px; text-transform: uppercase; }
                      .expiration { background-color: #f5f5f5; border-left: 4px solid #667eea; padding: 15px; border-radius: 4px; font-size: 14px; color: #666; margin: 20px 0; }
                      .footer { text-align: center; margin-top: 40px; border-top: 1px solid #eee; padding-top: 20px; }
                      .footer-text { font-size: 12px; color: #999; }
                      .warning { background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 4px; padding: 12px; margin: 15px 0; font-size: 13px; color: #856404; }
                    </style>
                  </head>
                  <body>
                    <div class="container">
                      <div class="header">
                        <h2 style="margin: 0; color: #667eea;">CineBaianos</h2>
                        <p style="margin: 5px 0 0 0; color: #999;">Confirmação de Cadastro</p>
                      </div>
                      
                      <p>Olá, <b>%s</b>!</p>
                      <p>Bem-vindo ao <b>CineBaianos</b>! 🎬</p>
                      <p>Para concluir seu cadastro, use o código de verificação abaixo:</p>
                      
                      <div class="code-container">
                        <div class="code-label">Seu código de verificação</div>
                        <div class="code">%s</div>
                      </div>
                      
                      <div class="expiration">
                        ⏱️ <b>Importante:</b> Este código expira em <b>10 minutos</b>. Use-o antes que o tempo se esgote.
                      </div>
                      
                      <p>Se você não solicitou este cadastro, ignore este email. Nenhuma ação será necessária.</p>
                      
                      <div class="warning">
                        <b>Segurança:</b> Nunca compartilhe este código com ninguém. A CineBaianos nunca pedirá seu código por mensagem ou ligação.
                      </div>
                      
                      <div class="footer">
                        <p class="footer-text">CineBaianos &copy; 2026 | Todos os direitos reservados</p>
                        <p class="footer-text">Dúvidas? Entre em contato com nosso suporte</p>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(name, code);

        return Email.builder()
                .to(email)
                .subject(subject)
                .content(content)
                .build();
    }

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