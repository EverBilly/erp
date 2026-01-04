package com.pos.permiso.repository;

import com.pos.permiso.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    
    Optional<Permiso> findByCodigo(String codigo);
    
    List<Permiso> findByModulo(String modulo);
    
    List<Permiso> findByCategoria(String categoria);
    
    List<Permiso> findByActivoTrue();
    
    @Query("SELECT p FROM Permiso p WHERE p.codigo IN :codigos AND p.activo = true")
    List<Permiso> findByCodigos(@Param("codigos") Set<String> codigos);
}