package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
 
@Getter
@Setter
public class ActualizarPerfilRepartidorAdminDTO extends ActualizarPerfilAdminDTO {
 
    @NotBlank
    private String vehiculo;
 
    @NotBlank
    @Pattern(
            regexp = "(?i)^([A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2})$",
            message = "Patente inválida. Formato esperado: AAA999 o AA999AA"
    )
    private String patente;
 
    @NotBlank
    private String zona;
}
