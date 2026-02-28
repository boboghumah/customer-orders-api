package com.example.customerordersapi.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message){
        super(message);
    }

}
