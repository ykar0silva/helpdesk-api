package br.com.helpTI.helpdeskapi.exception;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(Object id) {
        super("Recurso não encontrado. ID: " + id);
    }
}