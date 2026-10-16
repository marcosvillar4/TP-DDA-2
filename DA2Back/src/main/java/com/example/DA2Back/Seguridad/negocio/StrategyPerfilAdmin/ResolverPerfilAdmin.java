package com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.Rol;
 
/**
 * Registro rol -> estrategia. Spring inyecta todos los IPerfilAdmin disponibles;
 * si dos declaran el mismo rol, la app falla al arrancar (mejor que fallar en runtime).
 * Misma idea que ResolverEstadoUsuario con los estados.
 */
@Component
public class ResolverPerfilAdmin {
 
    private final Map<Rol, IPerfilAdmin> perfilesPorRol;
 
    public ResolverPerfilAdmin(List<IPerfilAdmin> perfiles) {
        this.perfilesPorRol = perfiles.stream()
                .collect(Collectors.toMap(IPerfilAdmin::rol, Function.identity()));
    }
 
    /** Vacío para roles sin datos propios (ADMIN). */
    public Optional<IPerfilAdmin> resolver(Rol rol) {
        return Optional.ofNullable(perfilesPorRol.get(rol));
    }
}
