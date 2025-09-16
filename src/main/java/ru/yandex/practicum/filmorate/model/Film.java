package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;


@Data
@Builder
public class Film {

    private final Set<Genre> genres = new LinkedHashSet<>();
    private final Set<Director> directors = new LinkedHashSet<>();
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Duration duration;
    private Mpa mpa;
}
