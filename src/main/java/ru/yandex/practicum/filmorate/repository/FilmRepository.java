package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.mapper.DirectorFilmRowMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Slf4j
@Repository
public class FilmRepository extends BaseRepository<Film> {

    private final DirectorFilmRowMapper directorFilmRowMapper;
    private final RowMapper<Film> filmWithLikesRowMapper;

    public FilmRepository(JdbcTemplate jdbc, @Qualifier("filmRowMapper") RowMapper<Film> rowMapper, DirectorFilmRowMapper directorFilmRowMapper, RowMapper<Film> filmWithLikesRowMapper) {
        super(jdbc, rowMapper);
        this.directorFilmRowMapper = directorFilmRowMapper;
        this.filmWithLikesRowMapper = filmWithLikesRowMapper;
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
                       + " mpa_id = ?"
                       + " where id = ?";
        int result = update(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (result == 0) {
            throw new EntityUpdateErrorException("Не удалось обновить пользователя");
        }
        return film;
    }

    public void deleteFilm(int id) {
        String queryLikes = "delete from likes where film_id = ?";
        jdbc.update(queryLikes, id);

        String queryGenres = "delete from film_genres where film_id = ?";
        jdbc.update(queryGenres, id);

        String queryFilms = "delete from films where id = ?";
        jdbc.update(queryFilms, id);
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

    public List<Film> getMostPopular(Integer count, Integer genreId, Integer year) {
        String onlyPopularQuery = """
                select f.*, coalesce(q.like_count, 0) AS counter, m.name as mpa_name
                from films f
                left join mpa m on f.mpa_id = m.id
                left join (
                    select film_id, count(user_id) as like_count
                    from likes
                    group by film_id
                ) q on q.film_id = f.id
                order by counter desc
                limit ?
                """;
        String baseQuery = """
                select f.*, coalesce(q.like_count, 0) AS counter, m.name AS mpa_name
                from films f
                left join mpa m ON f.mpa_id = m.id
                left join film_genres fg ON f.id = fg.film_id
                left join genres g ON fg.genre_id = g.id
                left join (
                    select film_id, count(user_id) AS like_count
                    from likes
                    group by film_id
                    ) q ON q.film_id = f.id
                """;
        String onlyGenreQuery = baseQuery + """
                where fg.genre_id = ?
                order by counter DESC
                limit ?
                """;
        String onlyYearQuery = baseQuery + """
                where EXTRACT(YEAR FROM f.release_date) = ?
                order by counter DESC
                limit ?
                """;
        String genreYearQuery = baseQuery + """
                where fg.genre_id = ?
                    AND EXTRACT(YEAR FROM f.release_date) = ?
                order by counter DESC
                limit ?
                """;
        if (year == null && genreId == null) {
            return getRecords(onlyPopularQuery, count);
        } else if (genreId == null) {
            return getRecords(onlyYearQuery, year, count);
        } else if (year == null) {
            return getRecords(onlyGenreQuery, genreId, count);
        }
        return getRecords(genreYearQuery, genreId, year, count);
    }

    public List<Film> getRecommendations(int id) {
        String query = """
                select f.*, m.name AS mpa_name
                from films f
                left join mpa m ON f.mpa_id = m.id
                inner join (
                    select l.film_id, count(l.user_id) AS counter
                    from likes l
                    where l.film_id IN (
                    select distinct l2.film_id
                    from likes l2
                    where l2.user_id IN (
                        select l3.user_id
                        from likes l1
                        join likes l3 ON l1.film_id = l3.film_id
                        where l1.user_id = ?
                        AND l3.user_id <> ?
                        group by l3.user_id
                        order by count(*) DESC
                        limit 5
                        )
                    AND l2.film_id NOT IN (
                        select film_id from likes where user_id = ?
                        )
                    limit 20
                    )
                    group by l.film_id
                    order by counter DESC
                ) q ON q.film_id = f.id
                order by q.counter DESC;
                """;
        return getRecords(query, id, id, id);
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

    private static final String BASE_FILM_DIRECTOR_QUERY = """
    SELECT f.id AS film_id, f.name, f.description, f.release_date, f.duration,
           f.mpa_id, m.name AS mpa_name,
           COALESCE(lc.like_count, 0) AS like_count
    FROM films AS f
    INNER JOIN mpa AS m ON f.mpa_id = m.id
    INNER JOIN film_directors AS fd ON f.id = fd.film_id
    LEFT JOIN (
        SELECT film_id, COUNT(user_id) AS like_count
        FROM likes
        GROUP BY film_id
    ) AS lc ON f.id = lc.film_id
    WHERE fd.director_id = ?
    """;

    private static final String ORDER_BY_YEAR = " ORDER BY f.release_date ASC";
    private static final String ORDER_BY_LIKES = " ORDER BY like_count DESC";

    public List<Film> getDirectorFilmsSortedByYear(int directorId) {
        String query = BASE_FILM_DIRECTOR_QUERY + ORDER_BY_YEAR;
        return jdbc.query(query, filmWithLikesRowMapper, directorId);
    }

    public List<Film> getDirectorFilmsSortedByLikes(int directorId) {
        String query = BASE_FILM_DIRECTOR_QUERY + ORDER_BY_LIKES;
        return jdbc.query(query, filmWithLikesRowMapper, directorId);
    }

    public List<Film> getDirectorFilmsSorted(int directorId, String sortBy) {
        if ("year".equalsIgnoreCase(sortBy)) {
            return getDirectorFilmsSortedByYear(directorId);
        } else {
            return getDirectorFilmsSortedByLikes(directorId);
        }
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

    public List<Film> searchFilms(String phrase, Set<String> fields) {

        String query = """
                select f.*, m.name AS mpa_name
                from films f
                left join mpa m ON f.mpa_id = m.id
                left join film_directors fd on f.id = fd.film_id
                left join directors d on d.id = fd.director_id
                """;
        String order = " order by f.id desc";
        String filmCondition = "LOWER(f.name) like LOWER(?)";
        String directorCondition = "LOWER(d.name) like LOWER(?)";

        if (fields.size() == 1 && fields.contains("title")) {
            query += (" WHERE " + filmCondition);
            query += order;
            return getRecords(query, "%" + phrase + "%");
        }

        if (fields.size() == 1 && fields.contains("director")) {
            query += (" WHERE " + directorCondition);
            query += order;
            return getRecords(query, "%" + phrase + "%");
        }

        if (fields.contains("title") && fields.contains("director")) {
            query += (" WHERE " + filmCondition + " OR " + directorCondition);
            query += order;
            return getRecords(query, "%" + phrase + "%", "%" + phrase + "%");
        }

        return getRecords(query);
    }
}
