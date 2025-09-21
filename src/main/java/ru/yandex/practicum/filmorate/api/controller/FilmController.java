package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.api.dto.CreateFilmRequest;
import ru.yandex.practicum.filmorate.api.dto.FilmDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateFilmRequest;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Validated
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmDTO> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public FilmDTO getFilm(
            @PathVariable
            @Positive(message = "id должен быть больше 0")
            int id) {
        return filmService.getFilm(id);
    }

    @PostMapping
    public FilmDTO createFilm(@Valid @RequestBody CreateFilmRequest film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public FilmDTO updateFilm(@Valid @RequestBody UpdateFilmRequest film) {
        return filmService.updateFilm(film);
    }


    @DeleteMapping("/{id}")
    public void deleteFilm(
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int id
    ) {
        filmService.deleteFilm(id);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDTO> getDirectorFilms(
            @PathVariable
            @Positive(message = "ID режиссера должен быть положительным числом")
            int directorId,
            @RequestParam(required = false) String sortBy) {
        return filmService.getDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<FilmDTO> getCommonFilms(
            @RequestParam
            @Positive(message = "userId должен быть целым числом больше 0")
            int userId,
            @RequestParam
            @Positive(message = "friendId должен быть целым числом больше 0")
            int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(
            @PathVariable
            @Positive(message = "film_id должен быть целым числом больше 0")
            int filmId,
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int userId
    ) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(
            @PathVariable
            @Positive(message = "film_id должен быть целым числом больше 0")
            int filmId,
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int userId
    ) {
        filmService.removeLike(filmId, userId);
    }


    @GetMapping("/popular")
    public List<FilmDTO> getMostPopular(
            @RequestParam(required = false, defaultValue = "1000")
            @Positive(message = "count должно быть целым числом больше 0")
            Integer count,
            @RequestParam(required = false)
            @Positive(message = "genreId должно быть целым числом больше 0")
            Integer genreId,
            @RequestParam(required = false)
            @Positive(message = "year должно быть целым числом больше 0")
            Integer year
    ) {
        log.info("сработал метод getMostPopular");
        return filmService.getMostPopular(count, genreId, year);
    }

    @GetMapping("/search")
    public List<FilmDTO> searchFilms(
                @RequestParam
                @NotBlank
                String query,
                @RequestParam
                @NotBlank
                String by
    ) {
        List<String> byFields = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase())
                .filter(s -> !s.isBlank())
                .toList();

        if (byFields.isEmpty()) {
            throw new ValidationException("Требуется указать минимум одно поле для поиска");
        }
        if (!byFields.contains("director") && !byFields.contains("title")) {
            throw new ValidationException("Для поиска требуется указать поле director и/или title");
        }

        return filmService.searchFilms(query, new HashSet<>(byFields));
    }
}