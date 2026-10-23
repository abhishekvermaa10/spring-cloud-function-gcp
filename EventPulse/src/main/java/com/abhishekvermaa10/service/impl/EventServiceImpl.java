package com.abhishekvermaa10.service.impl;

import com.abhishekvermaa10.model.ConferenceEvent;
import com.abhishekvermaa10.model.RegistrationRequest;
import com.abhishekvermaa10.model.RegistrationResponse;
import com.abhishekvermaa10.model.RegistrationStatus;
import com.abhishekvermaa10.repository.EventRepository;
import com.abhishekvermaa10.repository.RegistrationRepository;
import com.abhishekvermaa10.service.EventService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final RegistrationRepository registrationRepository;

    public EventServiceImpl(EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public List<ConferenceEvent> fetchEvents() {
        return eventRepository.findAll();
    }

    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        if (request == null) {
            return new RegistrationResponse(null, null, null, RegistrationStatus.INVALID_REQUEST);
        }
        if (request.eventId() == null || request.eventId().isBlank()
                || request.emailId() == null || request.emailId().isBlank()) {
            return new RegistrationResponse(null, request.eventId(), request.emailId(), RegistrationStatus.INVALID_REQUEST);
        }
        String eventId = request.eventId().trim();
        String emailId = request.emailId().trim();
        if (eventRepository.findById(eventId).isEmpty()) {
            return new RegistrationResponse(null, eventId, emailId, RegistrationStatus.EVENT_NOT_FOUND);
        }
        String registrationId = registrationRepository.findRegistrationId(eventId, emailId);
        if (registrationId != null) {
            return new RegistrationResponse(registrationId, eventId, emailId, RegistrationStatus.ALREADY_REGISTERED);
        } else {
            registrationId = registrationRepository.register(eventId, emailId);
            return new RegistrationResponse(registrationId, eventId, emailId, RegistrationStatus.REGISTERED);
        }
    }

}
