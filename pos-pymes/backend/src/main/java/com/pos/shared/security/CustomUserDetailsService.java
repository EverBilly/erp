package com.pos.shared.security;

import com.pos.usuario.model.Usuario;
import com.pos.shared.security.UserPrincipal;
import org.springframework.context.annotation.Primary;
import com.pos.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
            .orElseGet(() -> usuarioRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + usernameOrEmail)));

        return UserPrincipal.create(usuario);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));

        return UserPrincipal.create(usuario);
    }
}
