package com.abhishekvermaa10.model;

import java.time.LocalDate;

public record ConferenceEvent(
        String id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String location
) {
}
