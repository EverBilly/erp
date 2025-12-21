package com.pos.usuario.repository;

import com.pos.usuario.model.Usuario;  // <-- Import actualizado
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByEmailAndIdNot(String email, Long id);

    // Métodos adicionales
    long countByActivoTrue();
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    List<Usuario> findAllByActivoTrue();
}
