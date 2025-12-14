package com.brunoreolon.cinebaianosapi.util;

import com.brunoreolon.cinebaianosapi.domain.model.Email;
import com.brunoreolon.cinebaianosapi.domain.model.PasswordResetToken;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
                    <p>A senha da sua conta no <b>Cinebaianos</b> foi redefinida por um administrador do sistema.</p>
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

    public Email recoverPassword(User user, PasswordResetToken token) {
        String resetLink = String.format("%s/login.html?token=%s", frontendUrl, token.getToken());
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
}
