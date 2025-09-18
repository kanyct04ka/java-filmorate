package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;


@Repository
public class FilmRepository extends BaseRepository<Film> {

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> rowMapper) {
        super(jdbc, rowMapper);
    }

    public Film addFilm(Film film) {
        String query = "insert into films (name, description, release_date, duration, mpa_id)"
                       + " values (?, ?, ?, ?, ?)";
        int id = insert(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toSeconds(),
                film.getMpa().getId()
        ).intValue();
        film.setId(id);
        return film;
    }

    public Film updateFilm(Film film) {
        String query = "update films set"
                       + " name = ?,"
                       + " description = ?,"
                       + " release_date = ?,"
                       + " duration = ?,"
                       + " mpa_id = ?";
        int result = update(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());

        if (result == 0) {
            throw new EntityUpdateErrorException("Не удалось обновить пользователя");
        }
        return film;
    }

    public List<Film> getAllFilms() {
        String query = "select f.*, m.name as mpa_name"
                       + " from films f"
                       + " inner join mpa m on f.mpa_id = m.id";
        return getRecords(query);
    }

    public Optional<Film> getFilmById(int id) {
        String query = "select f.*, m.name as mpa_name"
                       + " from films f"
                       + " inner join mpa m on f.mpa_id = m.id"
                       + " where f.id = ?";
        return getRecord(query, id);
    }

    public void linkGenresToFilm(Film film, List<Genre> genres) {
        StringBuilder query = new StringBuilder("insert into film_genres (film_id, genre_id) values");
        for (int i = 0; i < genres.size(); i++) {
            query.append(" (%d, %d)".formatted(film.getId(), genres.get(i).getId()));
            if (i + 1 != genres.size()) {
                query.append(", ");
            }
        }
        jdbc.update(query.toString());
    }

    public void deleteLinkedGenres(int filmId) {
        String query = "delete from film_genres where film_id = ?";
        delete(query, filmId);
    }

    public void addLike(int filmId, int userId) {
        String query = "insert into likes (film_id, user_id) values (?, ?) ";
        jdbc.update(query, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String query = "delete from likes where film_id = ? and user_id = ?";
        jdbc.update(query, filmId, userId);
    }

    public List<Film> getTopLikedFilms(int count) {
        String query = "select f.*, m.name as mpa_name"
                       + " from films f"
                       + " left join mpa m on f.mpa_id = m.id"
                       + " inner join (select film_id, count(user_id) as counter"
                       + " from likes"
                       + " group by film_id"
                       + " order by count(user_id) desc"
                       + " limit ?) q on q.film_id = f.id"
                       + " order by q.counter desc";
        return getRecords(query, count);
    }

    public void deleteLinkedDirectors(int filmId) {
        String query = "DELETE FROM film_directors WHERE film_id = ?";
        delete(query, filmId);
    }

    public void linkDirectorsToFilm(int filmId, List<Integer> directorIds) {
        if (directorIds.isEmpty()) return;
        StringBuilder query = new StringBuilder("INSERT INTO film_directors (film_id, director_id) VALUES ");
        for (int i = 0; i < directorIds.size(); i++) {
            query.append("(?, ?)");
            if (i < directorIds.size() - 1) {
                query.append(", ");
            }
        }
        Object[] params = new Object[directorIds.size() * 2];
        for (int i = 0; i < directorIds.size(); i++) {
            params[i * 2] = filmId;
            params[i * 2 + 1] = directorIds.get(i);
        }
        jdbc.update(query.toString(), params);
    }

    public List<Film> getDirectorFilmsSorted(int directorId, String sortBy) {
        String orderByClause;
        if ("year".equalsIgnoreCase(sortBy)) {
            orderByClause = "f.release_date ASC";
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            orderByClause = "like_count DESC";
        } else {
            orderByClause = "f.id ASC";
        }

        String query = "SELECT f.*, m.name AS mpa_name, " +
                       "COALESCE(l.like_count, 0) AS like_count " +
                       "FROM films f " +
                       "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                       "LEFT JOIN ( " +
                       "    SELECT film_id, COUNT(user_id) AS like_count " +
                       "    FROM likes " +
                       "    GROUP BY film_id " +
                       ") l ON f.id = l.film_id " +
                       "INNER JOIN film_directors fd ON f.id = fd.film_id " +
                       "WHERE fd.director_id = ? " +
                       "ORDER BY " + orderByClause;

        return getRecords(query, directorId);
    }
}