package com.example.DA2Back.producto.negocio;

import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.producto.dto.ProductoCreateDTO;
import org.springframework.stereotype.Service;

@Service
public class ProductoService implements IProductoService {


    // Conexion con productRepo para la BDD
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto crearProducto(ProductoCreateDTO productoCreateDTO) {
        Producto producto = new Producto(productoCreateDTO.getNombre(), productoCreateDTO.getDescripcion());
        return productoRepository.save(producto);
    }


    public String test() {
        return "Hola desde el servicio de productos";
    }


}
