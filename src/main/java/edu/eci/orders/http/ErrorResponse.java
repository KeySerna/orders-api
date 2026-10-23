package edu.eci.orders.http;

/** Formato de error de la API: {"error": "...", "message": "..."} */
public record ErrorResponse(String error, String message) {
}