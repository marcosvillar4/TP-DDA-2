package com.example.DA2Back.comercio.comercioDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioCreateDTO {

   private String nombreComercial;
   private String razonSocial;
   private String direccion;
   private String cuit;
   private String telefono;
   private String email;
}
