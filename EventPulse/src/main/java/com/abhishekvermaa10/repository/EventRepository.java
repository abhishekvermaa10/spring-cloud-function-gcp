package com.abhishekvermaa10.repository;

import com.abhishekvermaa10.model.ConferenceEvent;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

    List<ConferenceEvent> findAll();

    Optional<ConferenceEvent> findById(String id);

}
