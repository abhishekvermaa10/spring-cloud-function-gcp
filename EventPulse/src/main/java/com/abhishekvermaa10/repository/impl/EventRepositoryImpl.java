package com.abhishekvermaa10.repository.impl;

import com.abhishekvermaa10.model.ConferenceEvent;
import com.abhishekvermaa10.repository.EventRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final Map<String, ConferenceEvent> events = Map.of(
            "EVT-101", new ConferenceEvent(
                    "EVT-101",
                    "Java2Days",
                    LocalDate.of(2022, 3, 8),
                    LocalDate.of(2022, 3, 11),
                    "Online"
            ),
            "EVT-102", new ConferenceEvent(
                    "EVT-102",
                    "Devoxx",
                    LocalDate.of(2026, 10, 5),
                    LocalDate.of(2026, 10, 9),
                    "Antwerp, Belgium"
            ),
            "EVT-103", new ConferenceEvent(
                    "EVT-103",
                    "GDG Cloud Chandigarh - DevFest & Code for Communities",
                    LocalDate.of(2026, 10, 23),
                    LocalDate.of(2026, 10, 24),
                    "Chandigarh, India"
            )
    );

    @Override
    public List<ConferenceEvent> findAll() {
        return List.copyOf(events.values());
    }

    @Override
    public Optional<ConferenceEvent> findById(String id) {
        return Optional.ofNullable(events.get(id));
    }

}
