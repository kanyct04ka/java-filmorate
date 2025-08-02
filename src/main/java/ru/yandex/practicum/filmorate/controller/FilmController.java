package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;

    @Autowired
    public FilmController(FilmStorage filmStorage,
                          FilmService filmService,
                          UserStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.userStorage = userStorage;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {

        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            logValidationError("Дата релиза не может быть раньше 28 декабря 1895 года");

        }

        if (film.getDuration() != null
                && film.getDuration().toSeconds() <= 0) {
            logValidationError("Продолжительность фильма должна быть положительным числом");
        }

        var addedFilm = filmStorage.addFilm(film);
        log.info("Фильм {} добавлен с ид={}", addedFilm.getName(), addedFilm.getId());
        return addedFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {

        if (film.getId() <= 0) {
            logValidationError("Id должен быть положительным числом");
        }

        if (filmStorage.getFilm(film.getId()).isEmpty()) {
            logNotFoundError("Фильм не найден");
        }

        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            logValidationError("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() != null
                && film.getDuration().toSeconds() <= 0) {
            logValidationError("Продолжительность фильма должна быть положительным числом");
        }

        filmStorage.updateFilm(film);
        log.info("Фильм с ид={} обновлен", film.getId());
        return film;
    }

    private void logValidationError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }

    private void logNotFoundError(String message) {
        log.error(message);
        throw new NotFoundIssueException(message);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        if (filmStorage.getFilm(id).isEmpty()) {
            logNotFoundError("Фильм не найден");
        }

        if (userStorage.getUser(userId).isEmpty()) {
            logNotFoundError("Юзер не найден");
        }

        filmService.addLike(
                filmStorage.getFilm(id).get(),
                userStorage.getUser(userId).get()
        );
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        if (filmStorage.getFilm(id).isEmpty()) {
            logNotFoundError("Фильм не найден");
        }

        if (userStorage.getUser(userId).isEmpty()) {
            logNotFoundError("Юзер не найден");
        }

        filmService.removeLike(
                filmStorage.getFilm(id).get(),
                userStorage.getUser(userId).get()
        );
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam int count) {
        if (count < 0) {
            logValidationError("Количество должно быть больше 0");
        }
        if (count == 0) {
            count = 10;
        }

        return filmService.getTopLikedFilms(count);
    }

}
