package com.hostel.smarthostel.exception;

/** Base class for violations of hostel business rules (HTTP 409). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) { super(message); }
}
