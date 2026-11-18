package com.hostel.smarthostel.exception;

public class OnlinePaymentNotConfiguredException extends RuntimeException {
    public OnlinePaymentNotConfiguredException() { super("Online payments are not configured"); }
}
