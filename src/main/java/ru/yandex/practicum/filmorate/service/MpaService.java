package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.MpaRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MpaService {
    private final MpaRepository mpaRepository;

    @Autowired
    public MpaService (MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public List<Mpa> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    public Mpa getMpaById(int id) {
        Optional<Mpa> mpa = mpaRepository.getMpaById(id);
        if (mpa.isEmpty()) {
            throw new NotFoundIssueException("Рейтинг не найден");
        }

        return mpa.get();
    }
}
