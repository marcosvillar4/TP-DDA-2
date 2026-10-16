package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import jakarta.validation.constraints.Email;   
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
 
@Getter
@Setter
public class ActualizarPerfilComercioAdminDTO extends ActualizarPerfilAdminDTO {
 
    @NotBlank
    private String nombreComercial;
 
    @NotBlank
    private String razonSocial;
 
    @NotBlank
    private String cuit;
 
    @NotBlank
    private String direccion;
 
    @NotBlank
    private String telefono;
 
    @NotBlank
    @Email
    private String email;
}
