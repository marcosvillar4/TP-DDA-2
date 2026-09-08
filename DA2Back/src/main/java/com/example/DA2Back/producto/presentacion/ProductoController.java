package com.example.DA2Back.producto.presentacion;

import com.example.DA2Back.producto.dto.ProductoResponseListDTO;
import com.example.DA2Back.producto.dto.ProductoCreateDTO;
import com.example.DA2Back.producto.dto.ProductoDeleteDTO;
import com.example.DA2Back.producto.dto.ProductoResponseDTO;
import com.example.DA2Back.producto.negocio.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/borrar")
    public ResponseEntity<String> borrarProducto(@RequestBody ProductoDeleteDTO productoDeleteDTO) {
        Long id = productoDeleteDTO.getId();
        int response = productoService.borrarProducto(id);
        System.out.println(response);
        if (response == 404) {
            return new ResponseEntity<String>("Producto no encontrado", HttpStatus.NOT_FOUND);
        } else if (response == 400) {
            return new ResponseEntity<String>("Producto no válido", HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<String>("Producto borrado correctamente", HttpStatus.OK);
        }
    }

    @GetMapping("")
    public ResponseEntity<ProductoResponseListDTO> getProductos() {
        ProductoResponseListDTO response = productoService.getAllProductos();
        return new ResponseEntity<ProductoResponseListDTO>(response, HttpStatusCode.valueOf(response.getStatus()));
    }

    @GetMapping("/test")
    public String test() {
        return productoService.test();
    }



    
}
