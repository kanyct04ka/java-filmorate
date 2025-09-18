package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirector(
            @PathVariable
            @Positive(message = "ID режиссера должен быть положительным числом")
            int id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Получен запрос на создание режиссера: {}", director.getName());
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Получен запрос на обновление режиссера с ID {}: {}", director.getId(), director.getName());
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content при успешном удалении
    public void deleteDirector(
            @PathVariable
            @Positive(message = "ID режиссера должен быть положительным числом")
            int id) {
        log.info("Получен запрос на удаление режиссера с ID {}", id);
        directorService.deleteDirector(id);
    }
}