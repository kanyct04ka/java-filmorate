package ru.yandex.practicum.filmorate.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmControllerTests {
    private FilmController filmController;

    @BeforeEach
    void prepareNewController() {
        filmController = new FilmController();
    }

    @Test
    void createFilm_Success() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();

        assertEquals(1, filmController.createFilm(filmForUpload).getId());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void createFilm_SuccessSomeFilms() {
        int x = 3;
        for (int i = 1; i <= x; i++) {
            Film filmForUpload = Film.builder()
                    .name("film name " + i)
                    .description("film desc " + i)
                    .releaseDate(LocalDate.of(2020 + i, 05,23))
                    .duration(Duration.ofMinutes(112L))
                    .build();
            filmController.createFilm(filmForUpload).getId();
        }

        assertEquals(x, filmController.getAllFilms().size());
    }

    @Test
    void createFilm_VeryOldReleaseDate() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(1223, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {
            filmController.createFilm(filmForUpload);
        });
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", e.getMessage());
        assertEquals(0, filmController.getAllFilms().size());
    }

    @Test
    void createFilm_NegativeDuration() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(-112L))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {
            filmController.createFilm(filmForUpload);
        });
        assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
        assertEquals(0, filmController.getAllFilms().size());
    }

/*
ТЕСТЫ ПОД РЕАЛИЗАЦИЮ ЧЕРЕЗ PATH ПАРАМЕТР

    @Test
    void updateFilm_Success() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.createFilm(filmForUpload);

        Film filmForUpdate = Film.builder()
                .id(filmForUpload.getId())
                .name("new name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.updateFilm(filmForUpdate.getId(), filmForUpdate);

        List<Film> list = new ArrayList<>(filmController.getAllFilms());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(filmForUpdate.getName(), list.get(0).getName());
    }

    @Test
    void updateFilm_InvalidIdInPath() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.createFilm(filmForUpload);

        Film filmForUpdate = Film.builder()
                .id(filmForUpload.getId() * -1)
                .name("new name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {
            filmController.updateFilm(filmForUpdate.getId(), filmForUpdate);
        });
        assertEquals("Id должен быть положительным числом", e.getMessage());

        List<Film> list = new ArrayList<>(filmController.getAllFilms());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(filmForUpload.getName(), list.get(0).getName());
    }

    @Test
    void updateFilm_DifferentIdInPathAndBody() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.createFilm(filmForUpload);

        Film filmForUpdate = Film.builder()
                .id(filmForUpload.getId())
                .name("new name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {
            filmController.updateFilm(filmForUpdate.getId() + 8, filmForUpdate);
        });
        assertEquals("Не совпадает id в теле сообщения", e.getMessage());

        List<Film> list = new ArrayList<>(filmController.getAllFilms());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(filmForUpload.getName(), list.get(0).getName());
    }
*/

    @Test
    void updateFilm_Success() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.createFilm(filmForUpload);

        Film filmForUpdate = Film.builder()
                .id(filmForUpload.getId())
                .name("new name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.updateFilm(filmForUpdate);

        List<Film> list = new ArrayList<>(filmController.getAllFilms());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(filmForUpdate.getName(), list.get(0).getName());
    }

    @Test
    void updateFilm_NegativeId() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.createFilm(filmForUpload);

        Film filmForUpdate = Film.builder()
                .id(filmForUpload.getId() * -1)
                .name("new name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {
                    filmController.updateFilm(filmForUpdate);
                });
        assertEquals("Id должен быть положительным числом", e.getMessage());

        List<Film> list = new ArrayList<>(filmController.getAllFilms());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(filmForUpload.getName(), list.get(0).getName());
    }

    @Test
    void updateFilm_NoSuchFilm() {
        Film filmForUpload = Film.builder()
                .name("film name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();
        filmController.createFilm(filmForUpload);

        Film filmForUpdate = Film.builder()
                .id(89)
                .name("new name")
                .description("film desc")
                .releaseDate(LocalDate.of(2023, 05,23))
                .duration(Duration.ofMinutes(112L))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {
                    filmController.updateFilm(filmForUpdate);
                });
        assertEquals("Фильм не найден", e.getMessage());

        List<Film> list = new ArrayList<>(filmController.getAllFilms());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(filmForUpload.getName(), list.get(0).getName());
    }

}
