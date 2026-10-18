package com.example.DA2Back.notificacion.negocio;

import com.example.DA2Back.notificacion.dato.CanalNotificacion;
import com.example.DA2Back.notificacion.dato.Notificacion;
import com.example.DA2Back.notificacion.dato.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
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
            MimeMessageHelper mensaje = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mensaje.setFrom(remitente);
            mensaje.setTo(notificacion.getDestinatarioEmail());
            mensaje.setSubject(notificacion.getTitulo());
            mensaje.setText(construirHtml(notificacion), true);
            mensaje.addInline("logired-logo", new ClassPathResource("logo.png"));
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
        return cargarPlantilla(notificacion)
                .replace("{{titulo}}", titulo)
                .replace("{{mensaje}}", mensaje);
    }

    private String cargarPlantilla(Notificacion notificacion) {
        String archivo = switch (notificacion.getTipo()) {
            case SOLICITUD_REGISTRO -> "solicitud-registro.html";
            case ACTUALIZACION_ESTADO_USUARIO -> "estado-usuario.html";
            case ACTUALIZACION_PEDIDO -> "actualizacion-pedido.html";
            case PEDIDO_ASIGNADO -> "pedido-asignado.html";
            case STOCK_BAJO -> "stock-bajo.html";
        };

        try {
            return new ClassPathResource("templates/email/" + archivo)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar la plantilla de correo " + archivo,
                    exception);
        }
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
