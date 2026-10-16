package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
 
/*
 * Cambios sobre los datos de rol de un usuario. Se discrimina por "rol" igual que
 * RegisterDTO. El servicio verifica que el tipo enviado coincida con el rol real del
 * usuario guardado: el cliente no decide a qué módulo se escribe.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "rol",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActualizarPerfilComercioAdminDTO.class, name = "COMERCIO"),
        @JsonSubTypes.Type(value = ActualizarPerfilRepartidorAdminDTO.class, name = "REPARTIDOR"),
        @JsonSubTypes.Type(value = ActualizarPerfilDepositoAdminDTO.class, name = "DEPOSITO")
})
@Getter
@Setter
public abstract class ActualizarPerfilAdminDTO {
 
    private Rol rol;
}