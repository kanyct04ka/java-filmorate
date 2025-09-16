package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.api.dto.DirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DirectorService {
    private final DirectorRepository directorRepository;

    @Autowired
    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public List<Director> getAllDirectors() {
        return directorRepository.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        Optional<Director> director = directorRepository.getDirectorById(id);
        if (director.isEmpty()) {
            throw new NotFoundIssueException("Режиссер не найден");
        }
        return director.get();
    }

    public Director createDirector(DirectorRequest directorRequest) {
        Director director = Director.builder()
                .name(directorRequest.getName())
                .build();
        return directorRepository.saveDirector(director);
    }

    public Director updateDirector(Director director) {
        if (directorRepository.getDirectorById(director.getId()).isEmpty()) {
            throw new NotFoundIssueException("Режиссер не найден");
        }
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(int id) {
        if (directorRepository.getDirectorById(id).isEmpty()) {
            throw new NotFoundIssueException("Режиссер не найден");
        }
        directorRepository.deleteDirector(id);
    }
}