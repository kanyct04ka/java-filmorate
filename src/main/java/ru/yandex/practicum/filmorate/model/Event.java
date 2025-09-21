package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Event {
    private long id;
    private final Instant timestamp;
    private final EventType type;
    private final EventOperation operation;
    private final User user;
    private final long entityId;
}
