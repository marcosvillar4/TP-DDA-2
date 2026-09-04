package com.example.DA2Back.producto.presentacion;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.producto.negocio.IProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final IProductoService productoService;
    
}
