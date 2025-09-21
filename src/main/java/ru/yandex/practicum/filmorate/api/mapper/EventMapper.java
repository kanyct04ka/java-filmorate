package ru.yandex.practicum.filmorate.api.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import ru.yandex.practicum.filmorate.api.dto.EventDTO;
import ru.yandex.practicum.filmorate.model.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static EventDTO mapToEventDTO(Event event) {
        return EventDTO.builder()
                .eventId(event.getId())
                .timestamp(event.getTimestamp().toEpochMilli())
                .eventType(event.getType())
                .operation(event.getOperation())
                .userId(event.getUser().getId())
                .entityId(event.getEntityId())
                .build();
    }
}
