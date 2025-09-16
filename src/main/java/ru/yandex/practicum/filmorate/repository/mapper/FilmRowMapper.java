package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final GenreService genreService;
    private final DirectorService directorService;

    @Autowired
    public FilmRowMapper(GenreService genreService, DirectorService directorService) {
        this.genreService = genreService;
        this.directorService = directorService;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(Duration.ofSeconds(resultSet.getLong("duration")))
                .mpa(Mpa.builder()
                        .id(resultSet.getInt("mpa_id"))
                        .name(resultSet.getString("mpa_name"))
                        .build())
                .build();

        film.getGenres().addAll(genreService.getFilmGenres(film.getId()));
        film.getDirectors().addAll(directorService.getFilmDirectors(film.getId()));
        return film;
    }
}
