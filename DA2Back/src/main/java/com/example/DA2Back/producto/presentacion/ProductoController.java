package com.example.DA2Back.producto.presentacion;

import com.example.DA2Back.producto.dto.ProductoCreateDTO;
import com.example.DA2Back.producto.negocio.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.producto.negocio.IProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping("/crear")
    public ResponseEntity<String> crearProducto(@RequestBody ProductoCreateDTO productoCreateDTO) {
        productoService.crearProducto(productoCreateDTO);
        return new ResponseEntity<String>("Producto guardado correctamente", HttpStatus.OK);
    }

    @GetMapping("/test")
    public String test() {
        return productoService.test();
    }

    
}
