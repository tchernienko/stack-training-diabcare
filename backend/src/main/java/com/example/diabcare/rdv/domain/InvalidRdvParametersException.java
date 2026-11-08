package com.example.diabcare.rdv.domain;

public class InvalidRdvParametersException extends RuntimeException {
    public InvalidRdvParametersException(String message) {
        super(message);
    }
}
