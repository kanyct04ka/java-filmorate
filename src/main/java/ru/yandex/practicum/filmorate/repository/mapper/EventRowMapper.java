package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Component
public class EventRowMapper implements RowMapper<Event> {
    private final UserRepository userRepository;

    @Autowired
    public EventRowMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .id(resultSet.getLong("id"))
                .timestamp(Instant.ofEpochMilli(resultSet.getLong("timestamp")))
                .type(EventType.valueOf(resultSet.getString("type")))
                .operation(EventOperation.valueOf(resultSet.getString("operation")))
                .user(userRepository.getUserById(resultSet.getInt("user_id")).orElseThrow())
                .entityId(resultSet.getLong("entity_id"))
                .build();
    }
}
