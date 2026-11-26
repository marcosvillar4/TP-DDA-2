package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.TemaInterfaz;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActualizarTemaDTO {

    @NotNull(message = "El tema es obligatorio")
    private TemaInterfaz tema;
}