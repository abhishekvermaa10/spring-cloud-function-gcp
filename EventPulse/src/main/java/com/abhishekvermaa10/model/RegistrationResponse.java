package com.abhishekvermaa10.model;

public record RegistrationResponse(
        String registrationId,
        String eventId,
        String emailId,
        RegistrationStatus status
) {
}
