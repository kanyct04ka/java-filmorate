package ru.yandex.practicum.filmorate.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectorRequest {
    @NotBlank
    private String name;
}