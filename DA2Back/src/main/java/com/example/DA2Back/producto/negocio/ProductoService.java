package com.example.DA2Back.producto.negocio;

import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.producto.dto.ProductoResponseListDTO;
import com.example.DA2Back.producto.dto.ProductoCreateDTO;
import com.example.DA2Back.producto.dto.ProductoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

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

    public int borrarProducto(Long productoId){

        if (!productoRepository.existsById(productoId)) {
            return 404;
        } else {
            try {
                productoRepository.deleteById(productoId);
            } catch (IllegalArgumentException e) {
                return 400;
            }
        }



        return 200;
    }

    public ProductoResponseListDTO getAllProductos() {

        List<ProductoResponseDTO> productos = new LinkedList<>();

        for (Producto producto : productoRepository.findAll()) {
            productos.add(new ProductoResponseDTO(producto.getId(), producto.getNombre(), producto.getDescripcion()));
        }

        ProductoResponseListDTO response = null;

        if (productos.isEmpty()) {
            response = new ProductoResponseListDTO(productos, "No se han encontrado productos", false, 404);
        } else {
            response = new ProductoResponseListDTO(productos, "Productos encontrados con exito", true, 200);
        }

        return response;
    }


    public String test() {
        return "Hola desde el servicio de productos";
    }


}
