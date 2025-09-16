package ru.yandex.practicum.filmorate.api.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import ru.yandex.practicum.filmorate.api.dto.CreateFilmRequest;
import ru.yandex.practicum.filmorate.api.dto.FilmDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(CreateFilmRequest filmRequest) {
        Film film = Film.builder()
                .name(filmRequest.getName())
                .description(filmRequest.getDescription())
                .releaseDate(filmRequest.getReleaseDate())
                .duration(filmRequest.getDuration())
                .mpa(filmRequest.getMpa())
                .build();

        film.getGenres().addAll(filmRequest.getGenres());
        // Добавляем режиссёров из DTO в модель Film
        film.getDirectors().addAll(filmRequest.getDirectors());
        return film;
    }

    public static Film mapToFilm(UpdateFilmRequest filmRequest) {
        Film film = Film.builder()
                .id(filmRequest.getId())
                .name(filmRequest.getName())
                .description(filmRequest.getDescription())
                .releaseDate(filmRequest.getReleaseDate())
                .duration(filmRequest.getDuration())
                .mpa(filmRequest.getMpa())
                .build();

        film.getGenres().addAll(filmRequest.getGenres());
        // Добавляем режиссёров из DTO в модель Film
        film.getDirectors().addAll(filmRequest.getDirectors());
        return film;
    }

    public static FilmDTO mapToFilmDto(Film film) {
        FilmDTO dto = FilmDTO.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .build();

        dto.getGenres().addAll(film.getGenres());
        // Добавление режиссёров из модели Film в DTO
        dto.getDirectors().addAll(film.getDirectors());
        return dto;
    }
}
