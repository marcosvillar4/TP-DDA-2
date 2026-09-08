package com.example.DA2Back.producto.dto;

import java.util.List;

public class ProductoResponseListDTO {

    private String message;

    private Boolean success;

    private int status;

    private List<ProductoResponseDTO> productos;

    public ProductoResponseListDTO(List<ProductoResponseDTO> productos, String message, Boolean success, int status) {
        this.productos = productos;
        this.success = success;
        this.message = message;
        this.status = status;
    }

    public List<ProductoResponseDTO> getProductos() {
        return productos;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }
}
