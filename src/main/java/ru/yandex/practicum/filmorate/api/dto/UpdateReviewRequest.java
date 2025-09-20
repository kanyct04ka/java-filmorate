package ru.yandex.practicum.filmorate.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @Positive
    private final int reviewId;

    @Positive
    private final int filmId;

    @Positive
    private final int userId;

    @NotBlank
    @Size(max = 500)
    private final String content;

    @NotNull()
    private final Boolean isPositive;

    @NotNull
    private final int useful;
}
