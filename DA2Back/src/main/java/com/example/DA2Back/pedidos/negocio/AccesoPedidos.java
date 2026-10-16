package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.pedidos.dato.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccesoPedidos {
    private final ComercioRepository comercios;
    private final DepositoRepository depositos;

    public Alcance resolver(Usuario usuario, Rol... rolesPermitidos) {
        if (usuario == null || usuario.getId() == null || !usuario.isEnabled()
                || !Arrays.asList(rolesPermitidos).contains(usuario.getRol())) {
            throw new AccessDeniedException("No tiene permiso para operar pedidos");
        }
        return switch (usuario.getRol()) {
            case ADMIN -> new Alcance(null, null);
            case COMERCIO -> {
                var comercio = comercios.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("Usuario sin comercio asociado"));
                yield new Alcance(comercio.getId(), null);
            }
            case DEPOSITO -> {
                var deposito = depositos.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("Usuario sin deposito asociado"));
                if (deposito.getComercio() == null || deposito.getComercio().getId() == null) {
                    throw new AccessDeniedException("Deposito sin comercio asociado");
                }
                yield new Alcance(deposito.getComercio().getId(), deposito.getId());
            }
            default -> throw new AccessDeniedException("Utilice los endpoints de su perfil");
        };
    }

    // Solo ADMIN tiene ambos limites nulos. Los demas se resuelven desde usuarioId.
    public record Alcance(Long comercioId, Long depositoId) {
        public void validarComercio(Long id) {
            if (comercioId != null && !Objects.equals(comercioId, id)) {
                throw new AccessDeniedException("No tiene acceso al comercio solicitado");
            }
        }

        public void validarPedido(Pedido pedido) {
            validarComercio(pedido.getComercioId());
            if (depositoId != null && !Objects.equals(depositoId, pedido.getDepositoId())) {
                throw new AccessDeniedException("No tiene acceso al pedido solicitado");
            }
        }
    }
}
