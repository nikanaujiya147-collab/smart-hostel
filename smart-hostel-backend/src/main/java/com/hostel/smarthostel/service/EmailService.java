package com.hostel.smarthostel.service;

import com.hostel.smarthostel.model.User;

public interface EmailService {
    void sendWelcomeEmail(User user);
}
