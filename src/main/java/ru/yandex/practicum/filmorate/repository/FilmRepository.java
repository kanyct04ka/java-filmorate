package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;
import ru.yandex.practicum.filmorate.model.Film;

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
        String query = "select * from films";
        return getRecords(query);
    }

    public Optional<Film> getFilmById(int id) {
        String query = "select * from films where id = ?";
        return getRecord(query, id);
    }

    public void linkGenreToFilm(int filmId, int genreId) {
        String query = "insert into film_genres (film_id, genre_id) values (?, ?)";
        jdbc.update(query, filmId, genreId);
    }

    public void deleteLinkedGenres(int filmId) {
        String query = "delete from film_genres where film_id = ?";
        delete(query, filmId);
    }

    public void addLike(int filmId, int userId) {
        String query = "insert into likes (film_id, user_id) values (?, ?)";
        jdbc.update(query, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String query = "delete from likes where film_id = ? and user_id = ?";
        jdbc.update(query, filmId, userId);
    }

    public List<Integer> getTopLikedFilms(int count) {
        String query = "select film_id from likes "
                + "group by film_id "
                + "order by count(user_id) desc "
                + "limit ?";
        return jdbc.queryForList(query, Integer.class, count);
    }
}
