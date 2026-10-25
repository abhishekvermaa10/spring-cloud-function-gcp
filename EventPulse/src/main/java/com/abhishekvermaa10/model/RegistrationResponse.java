package com.abhishekvermaa10.model;

import com.abhishekvermaa10.enums.RegistrationStatus;

public record RegistrationResponse(
        String registrationId,
        Integer eventId,
        String emailId,
        RegistrationStatus status
) {
}
