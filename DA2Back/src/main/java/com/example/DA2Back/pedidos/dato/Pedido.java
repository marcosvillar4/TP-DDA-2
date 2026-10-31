package com.example.DA2Back.pedidos.dato;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.DA2Back.repartidor.dato.Repartidor;

/**
 * Entidad JPA que representa un Pedido en el sistema de logistica
 * de ultima milla. Mapeada a la tabla pedidos de la BD marketplace.
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comercio_id", nullable = false)
    private Long comercioId;

    @Column(name = "deposito_id", nullable = false)
    private Long depositoId;

    @Column(name = "direccion_destino", nullable = false)
    private String direccionDestino;

    @Column(name = "direccion_origen")
    private String direccionOrigen;

    @Column(name = "fecha_creacion", nullable = false)
    private java.time.LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado;

    @ManyToOne
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;

    // --- Cotizacion y ubicacion (todos nullable: los pedidos viejos no los tienen) ---

    @Column(name = "origen_lat")
    private Double origenLat;

    @Column(name = "origen_lng")
    private Double origenLng;

    @Column(name = "destino_lat")
    private Double destinoLat;

    @Column(name = "destino_lng")
    private Double destinoLng;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "duracion_estimada_min")
    private Integer duracionEstimadaMin;

    @Column(name = "cantidad_objetos")
    private Integer cantidadObjetos;

    @Column(name = "costo_envio", precision = 12, scale = 2)
    private java.math.BigDecimal costoEnvio;

    /** true si la distancia de la cotizacion fue estimada (sin servicio de ruteo real). */
    @Column(name = "cotizacion_estimada")
    private Boolean cotizacionEstimada;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<HistorialEstadoPedido> historial = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<DetallePedido> detalles = new java.util.ArrayList<>();
}
