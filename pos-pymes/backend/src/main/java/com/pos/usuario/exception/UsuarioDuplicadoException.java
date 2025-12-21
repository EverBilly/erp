package com.pos.usuario.exception;

/**
 * Excepción lanzada cuando se intenta crear/actualizar un usuario
 * con username o email que ya existe.
 *
 * El GlobalExceptionHandler la captura y retorna 409 Conflict.
 */
public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String campo, String valor) {
        super("Ya existe un usuario con " + campo + ": " + valor);
    }
}
