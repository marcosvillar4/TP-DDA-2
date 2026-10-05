package com.example.DA2Back.deposito.dto;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoResponseDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private Long comercioId; // null si todavía no fue asociado a un comercio
    private String comercioNombre;
    private Long usuarioId;
    private List<Long> itemsIds;

    /** Datos del usuario responsable del depósito (quien opera con rol DEPOSITO). */
    private String responsable;
    private String usuarioEmail;
    private EstadoUsuario usuarioEstado;

}
