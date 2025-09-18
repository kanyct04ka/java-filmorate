package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class FilmRepository extends BaseRepository<Film> {

    private final DirectorRepository directorRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public FilmRepository(JdbcTemplate jdbc,
                          RowMapper<Film> rowMapper,
                          DirectorRepository directorRepository,
                          GenreRepository genreRepository) {
        super(jdbc, rowMapper);
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
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
        List<Film> films = getRecords(query);

        films.forEach(film -> {
            List<Director> directors = directorRepository.getDirectorsByFilmId(film.getId());
            film.getDirectors().addAll(directors);

            List<Genre> genres = genreRepository.getGenresByFilmId(film.getId());
            film.getGenres().addAll(genres);
        });

        return films;
    }

    public Optional<Film> getFilmById(int id) {
        String query = "select f.*, m.name as mpa_name"
                       + " from films f"
                       + " inner join mpa m on f.mpa_id = m.id"
                       + " where f.id = ?";
        Optional<Film> film = getRecord(query, id);

        film.ifPresent(f -> {
            List<Director> directors = directorRepository.getDirectorsByFilmId(f.getId());
            f.getDirectors().addAll(directors);

            List<Genre> genres = genreRepository.getGenresByFilmId(f.getId());
            f.getGenres().addAll(genres);
        });

        return film;
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

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        String query;
        Object[] params = {directorId};

        if ("year".equals(sortBy)) {
            query = "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "INNER JOIN mpa m ON f.mpa_id = m.id " +
                    "INNER JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date ASC";
        } else if ("likes".equals(sortBy)) {
            query = "SELECT f.*, m.name as mpa_name, " +
                    "COALESCE((SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id), 0) as like_count " +
                    "FROM films f " +
                    "INNER JOIN mpa m ON f.mpa_id = m.id " +
                    "INNER JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY like_count DESC";
        } else {
            query = "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "INNER JOIN mpa m ON f.mpa_id = m.id " +
                    "INNER JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.id";
        }

        List<Film> films = getRecords(query, params);
        enrichFilmsWithDetails(films);
        return films;
    }

    private void enrichFilmsWithDetails(List<Film> films) {
        films.forEach(this::enrichFilmWithDetails);
    }

    private void enrichFilmWithDetails(Film film) {
        List<Director> directors = directorRepository.getDirectorsByFilmId(film.getId());
        film.getDirectors().clear();
        film.getDirectors().addAll(directors);
        List<Genre> genres = genreRepository.getGenresByFilmId(film.getId());
        film.getGenres().clear();
        film.getGenres().addAll(genres);
        String likeQuery = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        Integer likeCount = jdbc.queryForObject(likeQuery, Integer.class, film.getId());
    }
}