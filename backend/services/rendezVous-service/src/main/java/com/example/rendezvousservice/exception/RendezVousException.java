package com.example.rendezvousservice.exception;

public class RendezVousException extends RuntimeException {
    public RendezVousException(String message) {
        super(message);
    }

    public RendezVousException(String message, Throwable cause) {
        super(message, cause);
    }
}
