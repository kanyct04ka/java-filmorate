package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;
import java.util.List;
import java.util.Optional;

@Slf4j
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
            log.error("Режиссер с ID {} не найден", id);
            throw new NotFoundIssueException("Режиссер не найден");
        }
        return director.get();
    }

    public Director createDirector(Director director) {
        // Можно добавить валидацию имени
        return directorRepository.addDirector(director);
    }

    public Director updateDirector(Director director) {
        // Проверка существования
        getDirectorById(director.getId()); // Бросит исключение, если не найден
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(int id) {
        // Можно добавить проверку, есть ли фильмы у режиссера
        boolean deleted = directorRepository.deleteDirector(id);
        if (!deleted) {
            log.warn("Режиссер с ID {} не был удален (возможно, не существовал)", id);
            // Или бросить исключение
        }
    }
}