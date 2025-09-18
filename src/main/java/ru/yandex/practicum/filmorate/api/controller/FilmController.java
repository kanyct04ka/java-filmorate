package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.api.dto.CreateFilmRequest;
import ru.yandex.practicum.filmorate.api.dto.FilmDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateFilmRequest;

import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;


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
    public ResponseEntity<FilmDTO> getFilm(
            @PathVariable
            @Positive(message = "id должен быть больше 0")
            int id) {
        FilmDTO film = filmService.getFilm(id);
        return ResponseEntity.ok(film);
    }

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@Valid @RequestBody CreateFilmRequest film) {
        FilmDTO createdFilm = filmService.addFilm(film);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
    }

    @PutMapping
    public ResponseEntity<FilmDTO> updateFilm(@Valid @RequestBody UpdateFilmRequest film) {
        FilmDTO updatedFilm = filmService.updateFilm(film);
        return ResponseEntity.ok(updatedFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Void> addLike(
            @PathVariable
            @Positive(message = "film_id должен быть целым числом больше 0")
            int filmId,
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int userId
    ) {
        filmService.addLike(filmId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable
            @Positive(message = "film_id должен быть целым числом больше 0")
            int filmId,
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int userId
    ) {
        filmService.removeLike(filmId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public List<FilmDTO> getPopularFilms(
            @RequestParam(defaultValue = "10")
            @Positive(message = "Количество должно быть целым числом больше 0")
            int count) {
        return filmService.getTopLikedFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDTO> getFilmsByDirector(
            @PathVariable
            @Positive(message = "directorId должен быть целым числом больше 0")
            int directorId,
            @RequestParam(required = false) String sortBy
    ) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}