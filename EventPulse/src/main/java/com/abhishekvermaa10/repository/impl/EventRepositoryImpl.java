package com.abhishekvermaa10.repository.impl;

import com.abhishekvermaa10.model.ConferenceEvent;
import com.abhishekvermaa10.repository.EventRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final List<ConferenceEvent> events = List.of(
            new ConferenceEvent(1, "Java2Days", "Java2Days", LocalDate.of(2022, 3, 8), LocalDate.of(2022, 3, 11), "Online"),
            new ConferenceEvent(2, "DevFest", "GDG Cloud Chandigarh", LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 24), "Chandigarh, India")
    );

    @Override
    public List<ConferenceEvent> findAll() {
        return events;
    }

    @Override
    public Optional<ConferenceEvent> findById(Integer id) {
        return events.stream()
                .filter(event -> event.id().equals(id))
                .findFirst();
    }

}
