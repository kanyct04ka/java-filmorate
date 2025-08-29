package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;

import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> rowMapper;

    protected Long insert(String query, Object... params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;},
                keyHolder);

        Number id = keyHolder.getKey();

        if (id != null) {
            return id.longValue();
        } else {
            throw new EntityUpdateErrorException("Не удалось добавить пользователя");
        }
    }

    protected Optional<T> getRecord(String query, Object... params) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(query, rowMapper, params));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    protected List<T> getRecords(String query, Object... params) {
        return jdbc.query(query, rowMapper, params);
    }

    protected int update(String query, Object... params) {
        return jdbc.update(query, params);
    }

    protected boolean delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        return rowsDeleted > 0;
    }
}
