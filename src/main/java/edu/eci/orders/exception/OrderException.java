package edu.eci.orders.exception;

/**
 * Error base de la aplicacion. Tiene un codigo de negocio (ej. ORDER_NOT_FOUND),
 * NO un codigo HTTP. Traducirlo a 404/400 es responsabilidad de otra capa.
 */
public abstract class OrderException extends RuntimeException {

    private final String errorCode;

    protected OrderException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}