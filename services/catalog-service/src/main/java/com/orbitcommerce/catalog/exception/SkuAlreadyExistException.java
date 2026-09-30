package com.orbitcommerce.catalog.exception;

public class SkuAlreadyExistException extends RuntimeException {
    public SkuAlreadyExistException(String message) {
        super(message);
    }
}
