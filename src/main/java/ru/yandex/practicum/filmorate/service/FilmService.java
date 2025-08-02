package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Film film, User user) {
        film.getWhoLikes().add(user.getId());
    }

    public void removeLike(Film film, User user) {
        film.getWhoLikes().remove(user.getId());
    }

    // метод универсальный для любого количества ТОП
    public List<Film> getTopLikedFilms(int quantity) {
        return filmStorage.getAllFilms()
                .stream()
                // сортируем по убыванию количества лайков
                .sorted((one, two) -> Integer.compare(two.getWhoLikes().size(), one.getWhoLikes().size()))
                .limit(quantity)
                .collect(Collectors.toList());
    }
}
