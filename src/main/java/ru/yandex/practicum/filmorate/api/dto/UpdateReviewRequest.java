package ru.yandex.practicum.filmorate.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @Positive
    private int reviewId;

    @Positive
    private int filmId;

    @Positive
    private int userId;

    @NotBlank
    @Size(max = 500)
    private String content;

    @NotNull()
    private Boolean isPositive;

    @NotNull
    private int useful;
}
