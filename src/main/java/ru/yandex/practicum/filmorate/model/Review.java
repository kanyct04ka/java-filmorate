package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    private int reviewId;
    private int filmId;
    private int userId;
    private String content;
    private Boolean isPositive;
    private int useful;
}
