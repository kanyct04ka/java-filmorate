package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<Director> getAllDirectors() {
        String query = "select * from directors order by id";
        return getRecords(query);
    }

    public Optional<Director> getDirectorById(int id) {
        String query = "select * from directors where id = ?";
        return getRecord(query, id);
    }

    public Director saveDirector(Director director) {
        String query = "insert into directors (name) values (?)";
        int id = insert(query, director.getName()).intValue();
        director.setId(id);
        return director;
    }

    public Director updateDirector(Director director) {
        String query = "update directors set name = ? where id = ?";
        int result = update(query, director.getName(), director.getId());
        if (result == 0) {
            throw new EntityUpdateErrorException("Не удалось обновить режиссера");
        }
        return director;
    }

    public boolean deleteDirector(int id) {
        String deleteLinksQuery = "delete from film_directors where director_id = ?";
        jdbc.update(deleteLinksQuery, id);

        String query = "delete from directors where id = ?";
        int rowsDeleted = jdbc.update(query, id);

        return rowsDeleted > 0;
    }

    // Получает список режиссеров, связанных с определенным фильмом
    public List<Director> getDirectorsByFilmId(int filmId) {
        String query = "select d.* from directors d" +
                       " inner join film_directors fd on fd.director_id = d.id" +
                       " where fd.film_id = ?";
        return getRecords(query, filmId);
    }

    //Связывает список режиссеров с фильмом, создавая записи в промежуточной таблице
    public void linkDirectorsToFilm(int filmId, List<Director> directors) {
        if (directors.isEmpty()) return;

        StringBuilder query = new StringBuilder("insert into film_directors (film_id, director_id) values");
        for (int i = 0; i < directors.size(); i++) {
            query.append(" (").append(filmId).append(", ").append(directors.get(i).getId()).append(")");
            if (i + 1 != directors.size()) {
                query.append(", ");
            }
        }
        jdbc.update(query.toString());
    }

    //Удаляет все связи режиссеров с указанным фильмом
    public void deleteLinkedDirectors(int filmId) {
        String query = "delete from film_directors where film_id = ?";
        delete(query, filmId);
    }
}