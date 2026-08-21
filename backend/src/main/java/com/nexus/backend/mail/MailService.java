package com.nexus.backend.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mail transacional (convite, redefinição de senha). Se desabilitado
 * (nexus.mail.enabled=false, usado em testes), apenas loga o conteúdo em vez
 * de enviar de verdade.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public MailService(
            JavaMailSender mailSender,
            @Value("${nexus.mail.enabled}") boolean enabled,
            @Value("${nexus.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void send(String to, String subject, String body) {
        if (!enabled) {
            log.info("[e-mail desabilitado] para={} assunto={}\n{}", to, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("E-mail enviado para {} (assunto: {})", to, subject);
        } catch (MailException ex) {
            log.error("Falha ao enviar e-mail para {}: {}", to, ex.getMessage());
        }
    }
}
