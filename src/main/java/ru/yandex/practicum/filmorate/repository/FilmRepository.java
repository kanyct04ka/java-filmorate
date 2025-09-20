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

    public List<Film> getCommonFilms(int userId, int friendId) {
        String query = "SELECT f.*, m.name AS mpa_name, c.like_count " +
                       "FROM films f " +
                       "JOIN mpa m ON f.mpa_id = m.id " +
                       "JOIN (SELECT film_id, COUNT(user_id) AS like_count " +
                       "      FROM likes " +
                       "      GROUP BY film_id) c ON c.film_id = f.id " +
                       "WHERE f.id IN (" +
                       "    SELECT l1.film_id " +
                       "    FROM likes l1 " +
                       "    WHERE l1.user_id = ? " +
                       "    AND l1.film_id IN (" +
                       "        SELECT l2.film_id " +
                       "        FROM likes l2 " +
                       "        WHERE l2.user_id = ?" +
                       "    )" +
                       ") " +
                       "ORDER BY c.like_count DESC";

        return getRecords(query, userId, friendId);
    }
}
