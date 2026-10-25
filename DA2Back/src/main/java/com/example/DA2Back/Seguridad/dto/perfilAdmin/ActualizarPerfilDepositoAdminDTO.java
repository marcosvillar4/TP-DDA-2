package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
 
@Getter
@Setter
public class ActualizarPerfilDepositoAdminDTO extends ActualizarPerfilAdminDTO {
 
    @NotBlank
    private String nombre;
 
    @NotBlank
    private String direccion;
}
 