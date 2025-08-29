package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public FilmRowMapper(MpaService mpaService, GenreService genreService) {
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(Duration.ofSeconds(resultSet.getLong("duration")))
                .mpa(mpaService.getMpaById(resultSet.getInt("mpa_id")))
                .build();

        film.getGenres().addAll(genreService.getFilmGenres(film.getId()));
        return film;
    }
}
