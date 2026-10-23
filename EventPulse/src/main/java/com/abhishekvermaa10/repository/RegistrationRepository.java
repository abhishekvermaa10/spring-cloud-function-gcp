package com.abhishekvermaa10.repository;

public interface RegistrationRepository {

    String findRegistrationId(String eventId, String emailId);

    String register(String eventId, String emailId);

}
