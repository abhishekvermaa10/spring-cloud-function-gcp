package com.abhishekvermaa10.service;

import com.abhishekvermaa10.model.ConferenceEvent;
import com.abhishekvermaa10.model.RegistrationRequest;
import com.abhishekvermaa10.model.RegistrationResponse;

import java.util.List;

public interface EventService {

    List<ConferenceEvent> fetchEvents();

    RegistrationResponse register(RegistrationRequest registrationRequest);

}
