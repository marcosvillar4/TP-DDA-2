package com.example.DA2Back.notificacion.negocio;

import com.example.DA2Back.notificacion.dato.CanalNotificacion;
import com.example.DA2Back.notificacion.dato.Notificacion;
import com.example.DA2Back.notificacion.dato.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final NotificacionRepository notificacionRepository;

    @Value("${app.mail.from:no-reply@da2back.local}")
    private String remitente;

    @Scheduled(fixedDelayString = "${app.mail.poll-interval-ms:30000}")
    public void procesarPendientes() {
        notificacionRepository
                .findTop50ByCanalAndEnviadaFalseOrderByFechaCreacionAsc(CanalNotificacion.EMAIL)
                .forEach(this::enviar);
    }

    @Transactional
    protected void enviar(Notificacion notificacion) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mensaje = new MimeMessageHelper(mimeMessage, "UTF-8");
            mensaje.setFrom(remitente);
            mensaje.setTo(notificacion.getDestinatarioEmail());
            mensaje.setSubject(notificacion.getTitulo());
            mensaje.setText(construirHtml(notificacion), true);
            mailSender.send(mimeMessage);

            notificacion.setEnviada(true);
            notificacionRepository.save(notificacion);
            log.info("Correo enviado para la notificación {}", notificacion.getId());
        } catch (MessagingException | RuntimeException exception) {
            log.error("No se pudo enviar la notificación {} a {}. Quedará pendiente para reintento.",
                    notificacion.getId(), notificacion.getDestinatarioEmail(), exception);
        }
    }

    private String construirHtml(Notificacion notificacion) {
        String titulo = escapar(notificacion.getTitulo());
        String mensaje = escapar(notificacion.getMensaje()).replace("\n", "<br>");

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                </head>
                <body style="margin:0;background:#f3f6fa;font-family:Arial,sans-serif;color:#243447;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                           style="background:#f3f6fa;padding:32px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                       style="max-width:600px;background:#ffffff;border-radius:12px;
                                              overflow:hidden;box-shadow:0 3px 12px rgba(31,45,61,.12);">
                                    <tr>
                                        <td style="background:#1769aa;padding:28px 32px;color:#ffffff;">
                                            <div style="font-size:13px;letter-spacing:1.5px;
                                                        text-transform:uppercase;opacity:.85;">
                                                LogiRed
                                            </div>
                                            <h1 style="margin:10px 0 0;font-size:25px;line-height:1.25;">
                                                %s
                                            </h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <p style="margin:0;font-size:16px;line-height:1.7;">
                                                %s
                                            </p>
                                            <div style="margin-top:28px;padding:16px 18px;
                                                        background:#eef6fc;border-left:4px solid #1769aa;
                                                        color:#36536b;font-size:13px;line-height:1.5;">
                                                Este mensaje fue generado automáticamente por LogiRed.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:20px 32px;background:#f8fafc;color:#718096;
                                                   font-size:12px;text-align:center;">
                                            No respondas a este correo.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(titulo, titulo, mensaje);
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
