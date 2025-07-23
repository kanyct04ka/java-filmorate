package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;


@Data
@Builder
public class Film {

    int id;
    @NotBlank // название не может быть пустым
    String name;
    @Size(max = 200) // максимальная длина описания — 200 символов
    String description;
    LocalDate releaseDate;
    Duration duration;
}
