package com.abhishekvermaa10.model;

import java.time.LocalDate;

public record ConferenceEvent(
        Integer id,
        String name,
        String organizerName,
        LocalDate startDate,
        LocalDate endDate,
        String location
) {
}
