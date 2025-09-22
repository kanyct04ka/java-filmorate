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
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;
    private final EventService eventService;

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       GenreRepository genreRepository,
                       MpaRepository mpaRepository,
                       UserRepository userRepository,
                       DirectorRepository directorRepository,
                       EventService eventService
    ) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
        this.userRepository = userRepository;
        this.directorRepository = directorRepository;
        this.eventService = eventService;
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
            throw new NotFoundIssueException("Ошибка получения фильма");
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
        List<Integer> directorIds = filmRequest.getDirectors().stream()
                .map(Director::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .toList();
        if (!directorIds.isEmpty()) {
            List<Director> allDirectors = directorRepository.getAllDirectors();
            List<Integer> existingDirectorIds = allDirectors.stream()
                    .map(Director::getId)
                    .toList();

            if (!existingDirectorIds.containsAll(directorIds)) {
                logNotFoundError("Указаны режиссеры, которых нет в базе");
            }
        }
        long directorsWithId = filmRequest.getDirectors().stream()
                .map(Director::getId)
                .filter(Objects::nonNull)
                .count();
        if (filmRequest.getDirectors().size() != directorsWithId) {
            logValidationError("Все указанные режиссеры должны иметь корректный ID.");
        }

        Film film = filmRepository.addFilm(FilmMapper.mapToFilm(filmRequest));
        if (!filmRequest.getGenres().isEmpty()) {
            filmRepository.linkGenresToFilm(film, new ArrayList<>(filmRequest.getGenres()));
        }
        if (!directorIds.isEmpty()) {
            filmRepository.linkDirectorsToFilm(film.getId(), directorIds);
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
        List<Integer> directorIds = filmRequest.getDirectors().stream()
                .map(Director::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .toList();

        if (!directorIds.isEmpty()) {
            List<Director> allDirectors = directorRepository.getAllDirectors();
            List<Integer> existingDirectorIds = allDirectors.stream()
                    .map(Director::getId)
                    .toList();

            if (!existingDirectorIds.containsAll(directorIds)) {
                logNotFoundError("Указаны режиссеры, которых нет в базе");
            }
        }
        long directorsWithId = filmRequest.getDirectors().stream()
                .map(Director::getId)
                .filter(Objects::nonNull)
                .count();
        if (filmRequest.getDirectors().size() != directorsWithId) {
            logValidationError("Все указанные режиссеры должны иметь корректный ID.");
        }

        Film film = filmRepository.updateFilm(FilmMapper.mapToFilm(filmRequest));
        filmRepository.deleteLinkedGenres(film.getId());
        if (!filmRequest.getGenres().isEmpty()) {
            filmRepository.linkGenresToFilm(film, new ArrayList<>(filmRequest.getGenres()));
        }
        filmRepository.deleteLinkedDirectors(film.getId());
        if (!directorIds.isEmpty()) {
            filmRepository.linkDirectorsToFilm(film.getId(), directorIds);
        }

        log.info("Фильм с ид={} обновлен", film.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public void deleteFilm(int id) {
        log.info("Запрос на удаление фильма с id = {}", id);

        Optional<Film> film = filmRepository.getFilmById(id);
        if (film.isEmpty()) {
            logNotFoundError("Фильм с id = " + id + " не найден");
        }

        filmRepository.deleteFilm(id);

        log.info("Фильм с id = {} успешно удален", id);
    }

    public void addLike(int filmId, int userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        filmRepository.addLike(filmId, userId);

        eventService.createEvent(Event.builder()
                .user(userRepository.getUserById(userId).orElseThrow())
                .entityId(filmId)
                .type(EventType.LIKE)
                .operation(EventOperation.ADD)
                .timestamp(Instant.now())
                .build()
        );
    }

    public void removeLike(int filmId, int userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        filmRepository.removeLike(filmId, userId);
        eventService.createEvent(Event.builder()
                .user(userRepository.getUserById(userId).orElseThrow())
                .entityId(filmId)
                .type(EventType.LIKE)
                .operation(EventOperation.REMOVE)
                .timestamp(Instant.now())
                .build()
        );
    }

    public List<FilmDTO> getCommonFilms(int userId, int friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

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

    public List<FilmDTO> getDirectorFilms(int directorId, String sortBy) {
        if (directorRepository.getDirectorById(directorId).isEmpty()) {
            log.error("Режиссер с ID {} не найден", directorId);
            throw new NotFoundIssueException("Режиссер не найден");
        }

        List<Film> films;
        if ("year".equalsIgnoreCase(sortBy)) {
            films = filmRepository.getDirectorFilmsSortedByYear(directorId);
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            films = filmRepository.getDirectorFilmsSortedByLikes(directorId);
        } else {
            films = filmRepository.getDirectorFilmsSortedByLikes(directorId);
        }

        for (Film film : films) {
            List<Genre> filmGenres = genreRepository.getGenresByFilmId(film.getId());
            film.getGenres().clear();
            film.getGenres().addAll(filmGenres);

            List<Director> filmDirectors = directorRepository.getDirectorsByFilmId(film.getId());
            film.getDirectors().clear();
            film.getDirectors().addAll(filmDirectors);
        }

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDTO> getMostPopular(Integer count, Integer genreId, Integer year) {
        return filmRepository.getMostPopular(count, genreId, year)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDTO> getRecommendations(int id) {
        return filmRepository.getRecommendations(id)
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

    public List<FilmDTO> searchFilms(String query, Set<String> fields) {
        return filmRepository.searchFilms(query, fields)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }
}