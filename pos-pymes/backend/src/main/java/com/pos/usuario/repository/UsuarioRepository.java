package com.pos.usuario.repository;

import com.pos.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByUsernameAndActivoTrue(String username);
    
    Optional<Usuario> findByEmailAndActivoTrue(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    // Método customizado para verificar email único excluyendo un id
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.email = :email AND u.id != :id")
    Boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.intentosLogin = u.intentosLogin + 1 WHERE u.id = :id")
    void incrementarIntentosFallidos(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.intentosLogin = 0, u.bloqueadoHasta = null WHERE u.id = :id")
    void resetIntentosFallidos(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.bloqueadoHasta = :bloqueadoHasta WHERE u.id = :id")
    void bloquearUsuario(@Param("id") Long id, @Param("bloqueadoHasta") LocalDateTime bloqueadoHasta);
    
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.ultimoLogin = :ultimoLogin WHERE u.id = :id")
    void actualizarUltimoLogin(@Param("id") Long id, @Param("ultimoLogin") LocalDateTime ultimoLogin);
}