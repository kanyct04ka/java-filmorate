package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.yandex.practicum.filmorate.model.Event;

@Repository
public class EventRepository extends BaseRepository<Event> {

    public EventRepository(JdbcTemplate jdbc, RowMapper<Event> rowMapper) {
        super(jdbc, rowMapper);
    }

    public Event saveEvent(Event event) {
        String query = "insert into events (timestamp, type, operation, user_id, entity_id)"
                       + " values (?, ?, ?, ?, ?)";

        long id = insert(query,
                event.getTimestamp().toEpochMilli(),
                event.getType().toString(),
                event.getOperation().toString(),
                event.getUser().getId(),
                event.getEntityId()
        );

        event.setId(id);
        return event;
    }

    public List<Event> getEventsByUserId(int id) {
        String query = "select * from events where user_id = ? order by timestamp asc, id asc";
        return getRecords(query, id);
    }
}