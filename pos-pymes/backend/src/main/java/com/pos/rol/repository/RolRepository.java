package com.pos.rol.repository;

import com.pos.rol.model.Rol;  // <-- Import actualizado
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    
    Optional<Rol> findByNombre(String nombre);
    
    List<Rol> findByActivoTrue();
    
    @Query("SELECT r FROM Rol r WHERE r.esSistema = true")
    List<Rol> findRolesSistema();
    
    @Query("SELECT r FROM Rol r WHERE r.nivelPrioridad > :minPriority AND r.activo = true ORDER BY r.nivelPrioridad DESC")
    List<Rol> findRolesConPrioridadMinima(@Param("minPriority") Integer minPriority);
}