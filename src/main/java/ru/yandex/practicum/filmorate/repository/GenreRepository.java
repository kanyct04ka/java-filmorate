package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;


@Repository
public class GenreRepository extends BaseRepository<Genre> {
    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<Genre> getAllGenres() {
        String query = "select * from genres order by id";
        return getRecords(query);
    }

    public Optional<Genre> getGenreById(int id) {
        String query = "select * from genres where id = ?";
        return getRecord(query, id);
    }

    public List<Genre> getGenresByFilmId(int id) {
        String query = "select g.id, g.name from genres g"
                + " inner join film_genres fg on fg.genre_id = g.id"
                + " where fg.film_id = ?";
        return getRecords(query, id);
    }
}
