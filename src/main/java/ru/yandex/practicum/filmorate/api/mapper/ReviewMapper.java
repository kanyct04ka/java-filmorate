package ru.yandex.practicum.filmorate.api.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.api.dto.CreateReviewRequest;
import ru.yandex.practicum.filmorate.api.dto.ReviewDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {
    public static Review mapToReview(CreateReviewRequest request) {
        return Review.builder()
                .filmId(request.getFilmId())
                .userId(request.getUserId())
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .useful(0)
                .build();
    }

    public static Review mapToReview(UpdateReviewRequest request) {
        return Review.builder()
                .reviewId(request.getReviewId())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .useful(request.getUseful())
                .build();
    }

    public static ReviewDTO mapToReviewDto(Review review) {
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .useful(review.getUseful())
                .build();
    }
}
