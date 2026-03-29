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

    Optional<Permiso> findByCode(String code);

    List<Permiso> findByModule(String module);

    List<Permiso> findByCategoria(String categoria);

    List<Permiso> findByActiveTrue();

    @Query("SELECT p FROM Permiso p WHERE p.code IN :codes AND p.active = true")
    List<Permiso> findByCodigos(@Param("codes") Set<String> codes);
}
