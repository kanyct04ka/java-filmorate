package ru.yandex.practicum.filmorate.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.serializator.DurationSerializator;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
public class CreateFilmRequest {

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    private LocalDate releaseDate;

    @JsonSerialize(using = DurationSerializator.class)
    private Duration duration;

    private Mpa mpa;

    private final Set<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));

    private final Set<Director> directors = new HashSet<>();
}
