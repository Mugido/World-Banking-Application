package com.uche.World.Banking.Application.service;

import com.uche.World.Banking.Application.payload.request.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);
}
