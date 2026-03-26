package com.pos.rol.repository;

import com.pos.rol.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByName(String name);

    List<Rol> findByActiveTrue();

    @Query("SELECT r FROM Rol r WHERE r.isSystem = true")
    List<Rol> findRolesSistema();

    @Query("SELECT r FROM Rol r WHERE r.priorityLevel > :minPriority AND r.active = true ORDER BY r.priorityLevel DESC")
    List<Rol> findRolesConPrioridadMinima(@Param("minPriority") Integer minPriority);
}
