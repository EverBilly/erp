package com.pos.usuario.service;

import com.pos.usuario.dto.ActualizarUsuarioRequest;
import com.pos.usuario.dto.CrearUsuarioRequest;
import com.pos.usuario.dto.UsuarioResponse;
import com.pos.usuario.exception.UsuarioDuplicadoException;
import com.pos.usuario.exception.UsuarioNotFoundException;
import com.pos.usuario.model.Usuario;
import com.pos.usuario.repository.UsuarioRepository;
import com.pos.rol.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioTest;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ADMIN");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolAdmin);

        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setUsername("jperez");
        usuarioTest.setPasswordHash("$2a$10$hashedPassword"); // Password hasheado
        usuarioTest.setEmail("jperez@empresa.com");
        usuarioTest.setNombreCompleto("Juan Pérez");
        usuarioTest.setActivo(true);
        usuarioTest.setFechaCreacion(LocalDateTime.now());
        usuarioTest.setRoles(roles);
    }

    @Test
    @DisplayName("listarUsuarios() debe retornar lista de UsuarioResponse")
    void listarUsuarios_debeRetornarListaDeUsuarioResponse() {
        // ARRANGE (Preparar)
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioTest));

        // ACT (Actuar)
        List<UsuarioResponse> resultado = usuarioService.listarUsuarios();

        // ASSERT (Verificar)
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        UsuarioResponse response = resultado.get(0);
        assertEquals("jperez", response.getUsername());
        assertEquals("jperez@empresa.com", response.getEmail());
        assertEquals("Juan", response.getNombre());
        assertEquals("Pérez", response.getApellido());
        assertTrue(response.isActivo());
        assertTrue(response.getRoles().contains("ADMIN"));
    }

    @Test
    @DisplayName("listarUsuarios() debe retornar lista vacía si no hay usuarios")
    void listarUsuarios_debeRetornarListaVaciaSiNoHayUsuarios() {
        // ARRANGE
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList());

        // ACT
        List<UsuarioResponse> resultado = usuarioService.listarUsuarios();

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("listarUsuarios() debe llamar al repository una vez")
    void listarUsuarios_debeLlamarAlRepositoryUnaVez() {
        // ARRANGE
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList());

        // ACT
        usuarioService.listarUsuarios();

        // ASSERT
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerUsuarioPorId() debe retornar UsuarioResponse cuando existe")
    void obtenerUsuarioPorId_debeRetornarUsuarioResponseCuandoExiste() {
        // ARRANGE
        Long idBuscado = 1L;
        when(usuarioRepository.findById(idBuscado)).thenReturn(Optional.of(usuarioTest));

        // ACT
        UsuarioResponse resultado = usuarioService.obtenerUsuarioPorId(idBuscado);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(idBuscado, resultado.getId());
        assertEquals("jperez", resultado.getUsername());
        assertEquals("jperez@empresa.com", resultado.getEmail());
        assertEquals("Juan", resultado.getNombre());
    }

    @Test
    @DisplayName("obtenerUsuarioPorId() debe lanzar excepción cuando no existe")
    void obtenerUsuarioPorId_debeLanzarExcepcionCuandoNoExiste() {
        // ARRANGE
        Long idInexistente = 999L;
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> usuarioService.obtenerUsuarioPorId(idInexistente)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    @DisplayName("obtenerUsuarioPorId() debe incluir roles del usuario")
    void obtenerUsuarioPorId_debeIncluirRolesDelUsuario() {
        // ARRANGE
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioTest));

        // ACT
        UsuarioResponse resultado = usuarioService.obtenerUsuarioPorId(1L);

        // ASSERT
        assertNotNull(resultado.getRoles());
        assertFalse(resultado.getRoles().isEmpty());
        assertTrue(resultado.getRoles().contains("ADMIN"));
    }

    @Test
    @DisplayName("crearUsuario() debe crear usuario y retornar UsuarioResponse")
    void crearUsuario_debeCrearUsuarioYRetornarResponse() {
        // ARRANGE
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("nuevo");
        request.setEmail("nuevo@empresa.com");
        request.setPassword("password123");
        request.setNombre("Nuevo");
        request.setApellido("Usuario");

        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("nuevo@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(2L);
            u.setFechaCreacion(LocalDateTime.now());
            return u;
        });

        // ACT
        UsuarioResponse resultado = usuarioService.crearUsuario(request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("nuevo", resultado.getUsername());
        assertEquals("nuevo@empresa.com", resultado.getEmail());
        assertEquals("Nuevo", resultado.getNombre());
    }

    @Test
    @DisplayName("crearUsuario() debe hashear el password")
    void crearUsuario_debeHashearPassword() {
        // ARRANGE
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("test");
        request.setEmail("test@empresa.com");
        request.setPassword("miPassword");
        request.setNombre("Test");
        request.setApellido("User");

        when(usuarioRepository.existsByUsername("test")).thenReturn(false);
        when(usuarioRepository.existsByEmail("test@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("miPassword")).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });

        // ACT
        usuarioService.crearUsuario(request);

        // ASSERT - Verificar que se llamó encode()
        verify(passwordEncoder, times(1)).encode("miPassword");

        // Capturar el usuario guardado para verificar password hasheado
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("$2a$10$encodedPassword", captor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("crearUsuario() debe lanzar excepción si username existe")
    void crearUsuario_debeLanzarExcepcionSiUsernameExiste() {
        // ARRANGE
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("jperez"); // Ya existe
        request.setEmail("otro@empresa.com");
        request.setPassword("password");
        request.setNombre("Juan");
        request.setApellido("Otro");

        when(usuarioRepository.existsByUsername("jperez")).thenReturn(true);

        // ACT & ASSERT
        UsuarioDuplicadoException exception = assertThrows(
            UsuarioDuplicadoException.class,
            () -> usuarioService.crearUsuario(request)
        );

        assertTrue(exception.getMessage().contains("username"));
    }

    @Test
    @DisplayName("crearUsuario() debe lanzar excepción si email existe")
    void crearUsuario_debeLanzarExcepcionSiEmailExiste() {
        // ARRANGE
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("nuevo");
        request.setEmail("jperez@empresa.com"); // Ya existe
        request.setPassword("password");
        request.setNombre("Nuevo");
        request.setApellido("Usuario");

        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("jperez@empresa.com")).thenReturn(true);

        // ACT & ASSERT
        UsuarioDuplicadoException exception = assertThrows(
            UsuarioDuplicadoException.class,
            () -> usuarioService.crearUsuario(request)
        );

        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    @DisplayName("actualizarUsuario() debe actualizar campos y retornar UsuarioResponse")
    void actualizarUsuario_debeActualizarCamposYRetornarResponse() {
        // ARRANGE
        Long idUsuario = 1L;
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Juan Carlos");
        request.setApellido("Pérez García");
        request.setEmail("jcarlos@empresa.com");

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioTest));
        when(usuarioRepository.existsByEmailAndIdNot("jcarlos@empresa.com", idUsuario)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        UsuarioResponse resultado = usuarioService.actualizarUsuario(idUsuario, request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Juan Carlos", resultado.getNombre());
        assertEquals("Pérez García", resultado.getApellido());
        assertEquals("jcarlos@empresa.com", resultado.getEmail());
    }

    @Test
    @DisplayName("actualizarUsuario() debe hashear password si se proporciona")
    void actualizarUsuario_debeHashearPasswordSiSeProporciona() {
        // ARRANGE
        Long idUsuario = 1L;
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setPassword("nuevoPassword123");

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioTest));
        when(passwordEncoder.encode("nuevoPassword123")).thenReturn("$2a$10$nuevoHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        usuarioService.actualizarUsuario(idUsuario, request);

        // ASSERT
        verify(passwordEncoder, times(1)).encode("nuevoPassword123");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("$2a$10$nuevoHash", captor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("actualizarUsuario() debe lanzar excepción si usuario no existe")
    void actualizarUsuario_debeLanzarExcepcionSiUsuarioNoExiste() {
        // ARRANGE
        Long idInexistente = 999L;
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Cualquier");

        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> usuarioService.actualizarUsuario(idInexistente, request)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    @DisplayName("desactivarUsuario() debe cambiar activo a false")
    void desactivarUsuario_debeCambiarActivoAFalse() {
        // ARRANGE
        Long idUsuario = 1L;
        usuarioTest.setActivo(true); // Usuario activo

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioTest));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        UsuarioResponse resultado = usuarioService.desactivarUsuario(idUsuario);

        // ASSERT
        assertNotNull(resultado);
        assertFalse(resultado.isActivo());
    }

    @Test
    @DisplayName("desactivarUsuario() debe lanzar excepción si usuario no existe")
    void desactivarUsuario_debeLanzarExcepcionSiNoExiste() {
        // ARRANGE
        Long idInexistente = 999L;
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> usuarioService.desactivarUsuario(idInexistente)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    @DisplayName("desactivarUsuario() debe guardar el cambio en el repository")
    void desactivarUsuario_debeGuardarCambioEnRepository() {
        // ARRANGE
        Long idUsuario = 1L;
        usuarioTest.setActivo(true);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioTest));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        usuarioService.desactivarUsuario(idUsuario);

        // ASSERT
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertFalse(captor.getValue().getActivo());
    }
}