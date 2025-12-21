package com.pos.usuario.exception;

/**
 * Excepción lanzada cuando no se encuentra un usuario.
 *
 * Extiende RuntimeException para no forzar try-catch en cada llamada.
 * El GlobalExceptionHandler la captura y retorna 404.
 */
public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(Long id) {
        super("Usuario no encontrado con id: " + id);
    }

    public UsuarioNotFoundException(String mensaje) {
        super(mensaje);
    }
}
