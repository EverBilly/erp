package com.pos.repositories;

import com.pos.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    
    // Métodos que faltaban
    long countByActivoTrue();
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    List<Usuario> findAllByActivoTrue();
}