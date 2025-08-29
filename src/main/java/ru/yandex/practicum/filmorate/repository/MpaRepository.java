package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;


@Repository
public class MpaRepository extends BaseRepository<Mpa> {
    public MpaRepository(JdbcTemplate jdbc, RowMapper<Mpa> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<Mpa> getAllMpa() {
        String query = "select * from mpa order by id";
        return getRecords(query);
    }

    public Optional<Mpa> getMpaById(int id) {
        String query = "select * from mpa where id = ?";
        return getRecord(query, id);
    }
}
