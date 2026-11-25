package com.example.DA2Back.notificacion.negocio;

import com.example.DA2Back.notificacion.dato.CanalNotificacion;
import com.example.DA2Back.notificacion.dato.Notificacion;
import com.example.DA2Back.notificacion.dato.NotificacionRepository;
import com.example.DA2Back.notificacion.dato.TipoNotificacion;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/** Envio de correos pendientes: plantillas por tipo, escape de HTML y reintento ante fallo del SMTP. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailNotificationSenderTest {

    @Mock private JavaMailSender mailSender;
    @Mock private NotificacionRepository notificacionRepository;

    private EmailNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new EmailNotificationSender(mailSender, notificacionRepository);
        ReflectionTestUtils.setField(sender, "remitente", "no-reply@logired.test");
        when(mailSender.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    @DisplayName("procesarPendientes envia cada correo pendiente y lo marca como enviado")
    void procesarPendientes_enviaYMarca() {
        Notificacion n1 = notificacion(1L, TipoNotificacion.PEDIDO_ASIGNADO, "Titulo 1", "Mensaje 1");
        Notificacion n2 = notificacion(2L, TipoNotificacion.STOCK_BAJO, "Titulo 2", "Mensaje 2");
        when(notificacionRepository.findTop50ByCanalAndEnviadaFalseOrderByFechaCreacionAsc(CanalNotificacion.EMAIL))
                .thenReturn(List.of(n1, n2));

        sender.procesarPendientes();

        verify(mailSender, times(2)).send(any(MimeMessage.class));
        assertTrue(n1.isEnviada());
        assertTrue(n2.isEnviada());
        verify(notificacionRepository).save(n1);
        verify(notificacionRepository).save(n2);
    }

    @Test
    @DisplayName("Sin correos pendientes no se envia nada")
    void procesarPendientes_sinPendientes() {
        when(notificacionRepository.findTop50ByCanalAndEnviadaFalseOrderByFechaCreacionAsc(CanalNotificacion.EMAIL))
                .thenReturn(List.of());

        sender.procesarPendientes();

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @EnumSource(TipoNotificacion.class)
    @DisplayName("Cada tipo de notificacion usa su plantilla y se envia al destinatario con el remitente configurado")
    void enviar_cadaTipo(TipoNotificacion tipo) throws Exception {
        Notificacion n = notificacion(1L, tipo, "Asunto " + tipo, "Cuerpo del mensaje");

        sender.enviar(n);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage enviado = captor.getValue();
        assertEquals("Asunto " + tipo, enviado.getSubject());
        assertEquals("destino@mail.com", enviado.getAllRecipients()[0].toString());
        assertEquals("no-reply@logired.test", enviado.getFrom()[0].toString());
        String html = htmlDe(enviado);
        assertNotNull(html);
        assertTrue(html.contains("Asunto " + tipo), "el titulo se inserta en la plantilla");
        assertTrue(html.contains("Cuerpo del mensaje"), "el mensaje se inserta en la plantilla");
        assertFalse(html.contains("{{titulo}}"));
        assertFalse(html.contains("{{mensaje}}"));
        assertTrue(n.isEnviada());
    }

    @Test
    @DisplayName("El HTML del usuario se escapa (anti XSS) y los saltos de linea pasan a <br>")
    void enviar_escapaHtml() throws Exception {
        Notificacion n = notificacion(1L, TipoNotificacion.STOCK_BAJO,
                "<script>alert('x')</script>", "Linea 1\nLinea 2 & \"comillas\"");

        sender.enviar(n);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        String html = htmlDe(captor.getValue());
        assertFalse(html.contains("<script>alert"), "no debe quedar HTML ejecutable del usuario");
        assertTrue(html.contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;"));
        assertTrue(html.contains("Linea 1<br>Linea 2 &amp; &quot;comillas&quot;"));
    }

    @Test
    @DisplayName("Mensaje nulo se envia como texto vacio sin romper el correo")
    void enviar_mensajeNulo() throws Exception {
        Notificacion n = notificacion(1L, TipoNotificacion.STOCK_BAJO, "Asunto", null);

        sender.enviar(n);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        assertFalse(htmlDe(captor.getValue()).contains("{{mensaje}}"));
        assertTrue(n.isEnviada());
    }

    @Test
    @DisplayName("Si el SMTP falla, la notificacion queda pendiente para reintento (no se marca ni se guarda)")
    void enviar_falloSmtp_quedaPendiente() {
        Notificacion n = notificacion(1L, TipoNotificacion.PEDIDO_ASIGNADO, "Asunto", "Mensaje");
        doThrow(new MailSendException("smtp caido")).when(mailSender).send(any(MimeMessage.class));

        sender.enviar(n);

        assertFalse(n.isEnviada());
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un fallo en un correo no impide procesar los siguientes")
    void procesarPendientes_unFalloNoCortaElLote() {
        Notificacion mala = notificacion(1L, TipoNotificacion.STOCK_BAJO, "A", "B");
        mala.setDestinatarioEmail(null); // setTo(null) falla
        Notificacion buena = notificacion(2L, TipoNotificacion.STOCK_BAJO, "C", "D");
        when(notificacionRepository.findTop50ByCanalAndEnviadaFalseOrderByFechaCreacionAsc(CanalNotificacion.EMAIL))
                .thenReturn(List.of(mala, buena));

        sender.procesarPendientes();

        assertFalse(mala.isEnviada());
        assertTrue(buena.isEnviada());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ───────────── helpers ─────────────

    private Notificacion notificacion(Long id, TipoNotificacion tipo, String titulo, String mensaje) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setTipo(tipo);
        n.setCanal(CanalNotificacion.EMAIL);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setDestinatarioEmail("destino@mail.com");
        n.setEnviada(false);
        return n;
    }

    /** Un MimeMessage sin enviar no tiene los Content-Type hasta saveChanges(): se llama antes de recorrerlo. */
    private static String htmlDe(MimeMessage mensaje) throws Exception {
        mensaje.saveChanges();
        return extraerHtml(mensaje);
    }

    private static String extraerHtml(Part parte) throws Exception {
        if (parte.isMimeType("text/html")) {
            return (String) parte.getContent();
        }
        if (parte.isMimeType("multipart/*")) {
            Multipart multi = (Multipart) parte.getContent();
            for (int i = 0; i < multi.getCount(); i++) {
                BodyPart hijo = multi.getBodyPart(i);
                String html = extraerHtml(hijo);
                if (html != null) {
                    return html;
                }
            }
        }
        return null;
    }
}
