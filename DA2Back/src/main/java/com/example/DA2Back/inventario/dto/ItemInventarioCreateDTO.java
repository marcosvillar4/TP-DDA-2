package com.example.DA2Back.inventario.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventarioCreateDTO {

    private Long inventarioId;

    private Long productoId;

    private Long depositoId;

    private Integer cantidad;
}
