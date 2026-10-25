package com.abhishekvermaa10.repository.impl;

import com.abhishekvermaa10.repository.RegistrationRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RegistrationRepositoryImpl implements RegistrationRepository {

    private final ConcurrentHashMap<String, String> registrations = new ConcurrentHashMap<>();

    @Override
    public String findRegistrationId(Integer eventId, String emailId) {
        return registrations.get(registrationKey(eventId, emailId));
    }

    @Override
    public String register(Integer eventId, String emailId) {
        String registrationId = UUID.randomUUID().toString();
        registrations.put(registrationKey(eventId, emailId), registrationId);
        return registrationId;
    }

    private String registrationKey(Integer eventId, String emailId) {
        return eventId + ":" + emailId.toLowerCase();
    }

}
