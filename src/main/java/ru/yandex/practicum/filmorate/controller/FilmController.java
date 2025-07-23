package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    private final Map<Integer, Film> films = new HashMap<>();
    private int counter = 0;

    private int getNextId() {
        return ++counter;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {

        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            logError("Дата релиза не может быть раньше 28 декабря 1895 года");

        }

        if (film.getDuration() != null
                && film.getDuration().toSeconds() <= 0) {
            logError("Продолжительность фильма должна быть положительным числом");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} добавлен с ид={}", film.getName(), film.getId());
        return film;
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable int id, @Valid @RequestBody Film film) {
        if (id <= 0) {
            logError("Id должен быть положительным числом");
        }

        if (id != film.getId()) {
            logError("Не совпадает id в теле сообщения");
        }

        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            logError("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() != null
                && film.getDuration().toSeconds() <= 0) {
            logError("Продолжительность фильма должна быть положительным числом");
        }

        films.put(id, film);
        log.info("Фильм с ид={} обновлен", film.getId());
        return film;
    }

    private void logError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }

}
