package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.api.dto.DirectorRequest;
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
    public ResponseEntity<Director> getDirector(
            @PathVariable
            @Positive(message = "id должен быть целым числом больше 0")
            int id
    ) {
        Director director = directorService.getDirectorById(id);
        return ResponseEntity.ok(director);
    }

    @PostMapping
    public ResponseEntity<Director> createDirector(@Valid @RequestBody DirectorRequest directorRequest) {
        Director director = directorService.createDirector(directorRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(director);
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody Director director) {
        Director updatedDirector = directorService.updateDirector(director);
        return ResponseEntity.ok(updatedDirector);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(
            @PathVariable
            @Positive(message = "id должен быть целым числом больше 0")
            int id
    ) {
        directorService.deleteDirector(id);
        return ResponseEntity.noContent().build();
    }
}