package edu.eci.orders.exception;

/** Se lanza cuando los datos recibidos no son validos. */
public class InvalidOrderException extends OrderException {

    public InvalidOrderException(String errorCode, String message) {
        super(errorCode, message);
    }
}