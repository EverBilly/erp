package com.pos.usuario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.usuario.dto.ActualizarUsuarioRequest;
import com.pos.usuario.dto.CrearUsuarioRequest;
import com.pos.usuario.dto.UsuarioResponse;
import com.pos.usuario.exception.UsuarioDuplicadoException;
import com.pos.usuario.exception.UsuarioNotFoundException;
import com.pos.usuario.service.UsuarioService;
import com.pos.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(GlobalExceptionHandler.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private UsuarioResponse usuarioResponse;

    @BeforeEach
    void setUp() {
        usuarioResponse = new UsuarioResponse(
            1L,
            "jperez",
            "jperez@empresa.com",
            "Juan",
            "Pérez",
            true,
            LocalDateTime.now(),
            null,
            Arrays.asList("ADMIN")
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios debe retornar lista de usuarios")
    void listarUsuarios_debeRetornarListaDeUsuarios() throws Exception {
        // ARRANGE
        List<UsuarioResponse> usuarios = Arrays.asList(usuarioResponse);
        when(usuarioService.listarUsuarios()).thenReturn(usuarios);

        // ACT & ASSERT
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].username", is("jperez")))
            .andExpect(jsonPath("$[0].email", is("jperez@empresa.com")));

        verify(usuarioService, times(1)).listarUsuarios();
    }

    @Test
    @DisplayName("GET /api/usuarios sin autenticación debe retornar 401")
    void listarUsuarios_sinAuth_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios/{id} debe retornar usuario")
    void obtenerUsuario_debeRetornarUsuario() throws Exception {
        // ARRANGE
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuarioResponse);

        // ACT & ASSERT
        mockMvc.perform(get("/api/usuarios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("jperez")))
            .andExpect(jsonPath("$.nombre", is("Juan")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios/{id} con ID inexistente debe retornar 404")
    void obtenerUsuario_idInexistente_debeRetornar404() throws Exception {
        // ARRANGE
        when(usuarioService.obtenerUsuarioPorId(999L))
            .thenThrow(new UsuarioNotFoundException(999L));

        // ACT & ASSERT
        mockMvc.perform(get("/api/usuarios/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/usuarios debe crear usuario y retornar 201")
    void crearUsuario_debeCrearYRetornar201() throws Exception {
        // ARRANGE
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("nuevo");
        request.setEmail("nuevo@empresa.com");
        request.setPassword("password123");
        request.setNombre("Nuevo");
        request.setApellido("Usuario");

        UsuarioResponse nuevoUsuario = new UsuarioResponse(
            2L, "nuevo", "nuevo@empresa.com", "Nuevo", "Usuario",
            true, LocalDateTime.now(), null, Arrays.asList()
        );

        when(usuarioService.crearUsuario(any(CrearUsuarioRequest.class))).thenReturn(nuevoUsuario);

        // ACT & ASSERT
        mockMvc.perform(post("/api/usuarios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username", is("nuevo")))
            .andExpect(jsonPath("$.email", is("nuevo@empresa.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/usuarios con datos inválidos debe retornar 400")
    void crearUsuario_datosInvalidos_debeRetornar400() throws Exception {
        // ARRANGE - Request sin campos obligatorios
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("ab"); // Muy corto (min 3)

        // ACT & ASSERT
        mockMvc.perform(post("/api/usuarios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/usuarios con username duplicado debe retornar 409")
    void crearUsuario_usernameDuplicado_debeRetornar409() throws Exception {
        // ARRANGE
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setUsername("jperez");
        request.setEmail("otro@empresa.com");
        request.setPassword("password123");
        request.setNombre("Juan");
        request.setApellido("Otro");

        when(usuarioService.crearUsuario(any(CrearUsuarioRequest.class)))
            .thenThrow(new UsuarioDuplicadoException("username", "jperez"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/usuarios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/usuarios/{id} debe actualizar y retornar 200")
    void actualizarUsuario_debeActualizarYRetornar200() throws Exception {
        // ARRANGE
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Juan Carlos");
        request.setApellido("Pérez García");

        UsuarioResponse actualizado = new UsuarioResponse(
            1L, "jperez", "jperez@empresa.com", "Juan Carlos", "Pérez García",
            true, LocalDateTime.now(), null, Arrays.asList("ADMIN")
        );

        when(usuarioService.actualizarUsuario(eq(1L), any(ActualizarUsuarioRequest.class)))
            .thenReturn(actualizado);

        // ACT & ASSERT
        mockMvc.perform(put("/api/usuarios/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre", is("Juan Carlos")))
            .andExpect(jsonPath("$.apellido", is("Pérez García")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/usuarios/{id} con ID inexistente debe retornar 404")
    void actualizarUsuario_idInexistente_debeRetornar404() throws Exception {
        // ARRANGE
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Cualquier");

        when(usuarioService.actualizarUsuario(eq(999L), any(ActualizarUsuarioRequest.class)))
            .thenThrow(new UsuarioNotFoundException(999L));

        // ACT & ASSERT
        mockMvc.perform(put("/api/usuarios/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/usuarios/{id}/desactivar debe desactivar y retornar 200")
    void desactivarUsuario_debeDesactivarYRetornar200() throws Exception {
        // ARRANGE
        UsuarioResponse desactivado = new UsuarioResponse(
            1L, "jperez", "jperez@empresa.com", "Juan", "Pérez",
            false, LocalDateTime.now(), null, Arrays.asList("ADMIN")
        );

        when(usuarioService.desactivarUsuario(1L)).thenReturn(desactivado);

        // ACT & ASSERT
        mockMvc.perform(patch("/api/usuarios/1/desactivar")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/usuarios/{id}/desactivar con ID inexistente debe retornar 404")
    void desactivarUsuario_idInexistente_debeRetornar404() throws Exception {
        // ARRANGE
        when(usuarioService.desactivarUsuario(999L))
            .thenThrow(new UsuarioNotFoundException(999L));

        // ACT & ASSERT
        mockMvc.perform(patch("/api/usuarios/999/desactivar")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }
}