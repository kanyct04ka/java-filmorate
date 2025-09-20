package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

}
