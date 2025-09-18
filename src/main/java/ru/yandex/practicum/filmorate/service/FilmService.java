package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.api.dto.CreateFilmRequest;
import ru.yandex.practicum.filmorate.api.dto.FilmDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.api.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.MpaRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;
    private final UserRepository userRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       GenreRepository genreRepository,
                       MpaRepository mpaRepository,
                       UserRepository userRepository
    ) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
        this.userRepository = userRepository;
    }

    public List<FilmDTO> getAllFilms() {
        return filmRepository.getAllFilms()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDTO getFilm(int id) {
        Optional<Film> film = filmRepository.getFilmById(id);
        if (film.isEmpty()) {
            throw new InternalErrorException("Ошибка получения фильма");
        }

        return FilmMapper.mapToFilmDto(film.get());
    }

    public FilmDTO addFilm(CreateFilmRequest filmRequest) {
        if (filmRequest.getReleaseDate() != null
                && filmRequest.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            logValidationError("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (filmRequest.getDuration() != null
                && filmRequest.getDuration().toSeconds() <= 0) {
            logValidationError("Продолжительность фильма должна быть положительным числом");
        }

        if (!mpaRepository.getAllMpa()
                .stream()
                .map(Mpa::getId)
                .toList()
                .contains(filmRequest.getMpa().getId())) {
            logNotFoundError("Указан рейтинг, которого нет в базе");
        }

        List<Genre> genres = genreRepository.getAllGenres();

        if (!filmRequest.getGenres()
                .stream()
                .map(Genre::getId)
                .filter(i -> !genres.stream()
                        .map(Genre::getId)
                        .toList()
                        .contains(i))
                .toList()
                .isEmpty()) {
            logNotFoundError("Указаны жанры, которых нет в базе");
        }

        Film film = filmRepository.addFilm(FilmMapper.mapToFilm(filmRequest));
        if (!filmRequest.getGenres().isEmpty()) {
            filmRepository.linkGenresToFilm(film, new ArrayList<>(filmRequest.getGenres()));
        }

        log.info("Фильм {} добавлен с ид={}", film.getName(), film.getId());

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDTO updateFilm(UpdateFilmRequest filmRequest) {

        checkFilmExists(filmRequest.getId());

        if (filmRequest.getReleaseDate() != null
                && filmRequest.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            logValidationError("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (filmRequest.getDuration() != null
                && filmRequest.getDuration().toSeconds() <= 0) {
            logValidationError("Продолжительность фильма должна быть положительным числом");
        }

        Film film = filmRepository.updateFilm(FilmMapper.mapToFilm(filmRequest));
        filmRepository.deleteLinkedGenres(film.getId());
        if (!filmRequest.getGenres().isEmpty()) {
            filmRepository.linkGenresToFilm(film, new ArrayList<>(filmRequest.getGenres()));
        }

        log.info("Фильм с ид={} обновлен", film.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public void addLike(int filmId, int userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        filmRepository.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        filmRepository.removeLike(filmId, userId);
    }

    public List<FilmDTO> getTopLikedFilms(int quantity) {
        return filmRepository.getTopLikedFilms(quantity)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private void logValidationError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }

    private void logNotFoundError(String message) {
        log.error(message);
        throw new NotFoundIssueException(message);
    }

    public List<FilmDTO> getCommonFilms(int userId, int friendId) {
        if (userRepository.getUserById(userId).isEmpty()) {
            logNotFoundError("Пользователь с ID " + userId + " не найден");
        }
        if (userRepository.getUserById(friendId).isEmpty()) {
            logNotFoundError("Пользователь с ID " + friendId + " не найден");
        }

        List<Film> commonFilms = filmRepository.getCommonFilms(userId, friendId);

        return commonFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    private void checkUserExists(int userId) {
        if (userRepository.getUserById(userId).isEmpty()) {
            logNotFoundError("Пользователь с ID " + userId + " не найден");
        }
    }

    private void checkFilmExists(int filmId) {
        if (filmRepository.getFilmById(filmId).isEmpty()) {
            logNotFoundError("Фильм с ID " + filmId + " не найден");
        }
    }
}
