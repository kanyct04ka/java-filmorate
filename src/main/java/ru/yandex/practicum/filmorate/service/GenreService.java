package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> getAllGenres() {
        return genreRepository.getAllGenres();
    }

    public Genre getGenreById(int id) {
        Optional<Genre> genre = genreRepository.getGenreById(id);
        if (genre.isEmpty()) {
            throw new NotFoundIssueException("Жанр не найден");
        }

        return genre.get();
    }

    public List<Genre> getFilmGenres(int filmId) {
        return genreRepository.getGenresByFilmId(filmId);
    }


}
