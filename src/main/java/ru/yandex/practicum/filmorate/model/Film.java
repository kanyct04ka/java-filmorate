package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.serializator.DurationSerializator;

import java.time.Duration;
import java.time.LocalDate;


@Data
@Builder
public class Film {

    int id;
    @NotBlank // название не может быть пустым
    private String name;
    @Size(max = 200) // максимальная длина описания — 200 символов
    private String description;
    private LocalDate releaseDate;
    @JsonSerialize(using = DurationSerializator.class)
    private Duration duration;
}
