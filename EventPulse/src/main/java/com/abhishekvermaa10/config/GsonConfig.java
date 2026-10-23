package com.abhishekvermaa10.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {

//    @Bean
//    public Gson gson() {
//        return new GsonBuilder()
//                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (date, type, context) -> context.serialize(date.format(DateTimeFormatter.ISO_LOCAL_DATE)))
//                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
//                .create();
//    }

}
