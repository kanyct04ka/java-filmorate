package ru.yandex.practicum.filmorate.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;


@Data
@Builder
public class EventDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long eventId;
    private final long timestamp;
    private final EventType eventType;
    private final EventOperation operation;
    private final int userId;
    private final long entityId;
}
