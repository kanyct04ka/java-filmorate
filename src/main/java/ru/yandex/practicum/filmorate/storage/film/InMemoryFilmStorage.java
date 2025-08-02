package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.ValidationInStorageException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private int counter = 0;

    private int getNextId() {
        return ++counter;
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() <= 0) {
            throw new ValidationInStorageException("Некорректный id фильма для обновления");
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public void deleteFilm(int id) {
        films.remove(id);
    }
}
