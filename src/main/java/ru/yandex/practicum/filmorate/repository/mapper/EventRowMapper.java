package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Component
public class EventRowMapper implements RowMapper<Event> {
    private final UserRepository userRepository;

    @Autowired
    public EventRowMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Optional<User> userOptional = userRepository.getUserById(resultSet.getInt("user_id"));

        if (userOptional.isEmpty()) {
            return null;
        }

        return Event.builder()
                .id(resultSet.getLong("id"))
                .timestamp(Instant.ofEpochMilli(resultSet.getLong("timestamp")))
                .type(EventType.valueOf(resultSet.getString("type")))
                .operation(EventOperation.valueOf(resultSet.getString("operation")))
                .user(userOptional.get())
                .entityId(resultSet.getLong("entity_id"))
                .build();
    }
}