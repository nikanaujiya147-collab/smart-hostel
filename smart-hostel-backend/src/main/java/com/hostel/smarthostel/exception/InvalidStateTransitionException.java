package com.hostel.smarthostel.exception;

/** Thrown by the State pattern when an action is not allowed in the current complaint state. */
public class InvalidStateTransitionException extends BusinessRuleException {
    public InvalidStateTransitionException(String message) { super(message); }
}
