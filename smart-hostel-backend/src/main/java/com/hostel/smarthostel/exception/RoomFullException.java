package com.hostel.smarthostel.exception;

/** A BusinessRuleException specialised for room capacity violations. */
public class RoomFullException extends BusinessRuleException {
    public RoomFullException(String roomNumber) {
        super("Room " + roomNumber + " is full");
    }
}
