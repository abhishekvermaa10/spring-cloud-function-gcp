package com.abhishekvermaa10.config;

import com.abhishekvermaa10.model.ConferenceEvent;
import com.abhishekvermaa10.model.RegistrationRequest;
import com.abhishekvermaa10.model.RegistrationResponse;
import com.abhishekvermaa10.enums.RegistrationStatus;
import com.abhishekvermaa10.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class FunctionConfig {

    private static final Logger logger = LoggerFactory.getLogger(FunctionConfig.class);

    @Bean
    public Supplier<List<ConferenceEvent>> publishEvents(EventService eventService) {
        return () -> {
            logger.info("Publishing events");
            List<ConferenceEvent> events = eventService.fetchEvents();
            logger.info("Published {} events", events.size());
            return events;
        };
    }

    @Bean
    public Function<RegistrationRequest, RegistrationResponse> registerParticipant(EventService eventService) {
        return request -> {
            logger.info("Received registration request: {}", request);
            RegistrationResponse response = eventService.register(request);
            logger.info("Registration result: {}", response);
            return response;
        };
    }

    @Bean
    public Consumer<RegistrationResponse> sendRegistrationEmail() {
        return response -> {
            logger.info("Received registration result for email processing: {}", response);
            if (response.status() == RegistrationStatus.REGISTERED) {
                logger.info("Mock email sent to {} for event {}. Registration ID: {}",
                        response.emailId(), response.eventId(), response.registrationId());
            } else {
                logger.info("Mock email skipped for participant {} and event {}. Registration status: {}",
                        response.emailId(), response.eventId(), response.status());
            }
        };
    }

}
