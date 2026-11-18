package com.hostel.smarthostel.exception;

/** Thrown when an entity with the given id does not exist (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
