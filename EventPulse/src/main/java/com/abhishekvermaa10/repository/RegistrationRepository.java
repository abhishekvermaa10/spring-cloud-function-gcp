package com.abhishekvermaa10.repository;

public interface RegistrationRepository {

    String findRegistrationId(Integer eventId, String emailId);

    String register(Integer eventId, String emailId);

}
