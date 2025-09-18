package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorRepository extends BaseRepository<Director> {

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> rowMapper) {
        super(jdbc, rowMapper);
    }

    public Director addDirector(Director director) {
        String query = "INSERT INTO directors (name) VALUES (?)";
        int id = insert(query, director.getName()).intValue();
        director.setId(id);
        return director;
    }

    public Director updateDirector(Director director) {
        String query = "UPDATE directors SET name = ? WHERE id = ?";
        int rowsUpdated = update(query, director.getName(), director.getId());
        if (rowsUpdated == 0) {
            throw new ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException("Не удалось обновить режиссера с id: " + director.getId());
        }
        return director;
    }

    public List<Director> getAllDirectors() {
        String query = "SELECT * FROM directors ORDER BY id";
        return getRecords(query);
    }

    public Optional<Director> getDirectorById(int id) {
        String query = "SELECT * FROM directors WHERE id = ?";
        return getRecord(query, id);
    }

    public boolean deleteDirector(int id) {
        String query = "DELETE FROM directors WHERE id = ?";
        return delete(query, id);
    }

    public void linkDirectorsToFilm(int filmId, List<Director> directors) {
        if (directors.isEmpty()) return;
        StringBuilder query = new StringBuilder("INSERT INTO film_directors (film_id, director_id) VALUES ");
        for (int i = 0; i < directors.size(); i++) {
            query.append("(?, ?)");
            if (i < directors.size() - 1) {
                query.append(", ");
            }
        }
        Object[] params = new Object[directors.size() * 2];
        for (int i = 0; i < directors.size(); i++) {
            params[i * 2] = filmId;
            params[i * 2 + 1] = directors.get(i).getId();
        }
        jdbc.update(query.toString(), params);
    }

    public void deleteLinkedDirectors(int filmId) {
        String query = "DELETE FROM film_directors WHERE film_id = ?";
        delete(query, filmId);
    }

    public List<Director> getDirectorsByFilmId(int filmId) {
        String query = "SELECT d.id, d.name FROM directors d " +
                       "INNER JOIN film_directors fd ON fd.director_id = d.id " +
                       "WHERE fd.film_id = ?";
        return getRecords(query, filmId);
    }
}