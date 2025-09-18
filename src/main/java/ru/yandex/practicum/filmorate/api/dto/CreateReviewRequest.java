package ru.yandex.practicum.filmorate.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewRequest {
    private int filmId;

    private int userId;

    @NotBlank
    @Size(max = 500)
    private String content;

    @NotNull
    private Boolean isPositive;
}
