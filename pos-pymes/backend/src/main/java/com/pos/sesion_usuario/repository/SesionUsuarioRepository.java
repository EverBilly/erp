package com.pos.sesion_usuario.repository;

import com.pos.sesion_usuario.model.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, UUID> {
    
    Optional<SesionUsuario> findByTokenActual(String token);
    
    Optional<SesionUsuario> findByTokenRefresh(String refreshToken);
    
    List<SesionUsuario> findByUsuarioIdAndActivaTrue(Long usuarioId);
    
    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.usuario.id = :usuarioId AND s.activa = true")
    void cerrarSesionesUsuario(@Param("usuarioId") Long usuarioId);
    
    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.fechaExpiracion < :now")
    void cerrarSesionesExpiradas(@Param("now") LocalDateTime now);
}