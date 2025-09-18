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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       GenreRepository genreRepository,
                       MpaRepository mpaRepository,
                       UserRepository userRepository,
                       DirectorRepository directorRepository
    ) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
        this.userRepository = userRepository;
        this.directorRepository = directorRepository;
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
        // --- Существующая валидация ---
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

        if (filmRepository.getFilmById(filmRequest.getId()).isEmpty()) {
            logNotFoundError("Фильм не найден");
        }

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

    public void addLike(int filmId, int userId) {
        if (filmRepository.getFilmById(filmId).isEmpty()) {
            logNotFoundError("Фильм не найден");
        }

        if (userRepository.getUserById(userId).isEmpty()) {
            logNotFoundError("Юзер не найден");
        }

        filmRepository.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        if (filmRepository.getFilmById(filmId).isEmpty()) {
            logNotFoundError("Фильм не найден");
        }

        if (userRepository.getUserById(userId).isEmpty()) {
            logNotFoundError("Юзер не найден");
        }

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

    public List<FilmDTO> getDirectorFilms(int directorId, String sortBy) {
        Optional<Director> directorOpt = directorRepository.getDirectorById(directorId);
        if (directorOpt.isEmpty()) {
            log.error("Режиссер с ID {} не найден при попытке получить его фильмы", directorId);
            throw new NotFoundIssueException("Режиссер не найден");
        }

        List<Film> films = filmRepository.getDirectorFilmsSorted(directorId, sortBy);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}