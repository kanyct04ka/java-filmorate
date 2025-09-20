package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@Builder
public class Film {

    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Duration duration;
    private Mpa mpa;
    private int likesCount;
    private final Set<Genre> genres = new HashSet<>();
    private final Set<Director> directors = new HashSet<>();
}
