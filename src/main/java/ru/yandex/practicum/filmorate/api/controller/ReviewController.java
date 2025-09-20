package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.api.dto.CreateReviewRequest;
import ru.yandex.practicum.filmorate.api.dto.ReviewDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewDTO> getReviews(@RequestParam(defaultValue = "0") @Positive(message = "id отзыва должно быть положительным") int filmId,
                                      @RequestParam(defaultValue = "10") @Positive(message = "id отзыва должно быть положительным") int count) {
        return reviewService.getReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public ReviewDTO getReview(@PathVariable @Positive(message = "id отзыва должно быть больше 0") int id) {
        return reviewService.getReview(id);
    }

    @PostMapping
    public ReviewDTO createReview(@Valid @RequestBody CreateReviewRequest createReviewRequest) {
        return reviewService.addReview(createReviewRequest);
    }

    @PutMapping
    public ReviewDTO updateReview(@Valid @RequestBody UpdateReviewRequest updateReviewRequest) {
        return reviewService.updateReview(updateReviewRequest);
    }

    @DeleteMapping("/{id}")
    public ReviewDTO deleteReview(@PathVariable @Positive(message = "id отзыва должно быть больше 0") int id) {
        return reviewService.deleteReview(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeReview(@PathVariable @Positive(message = "id отзыва должен быть больше 0") int id,
                              @PathVariable @Positive(message = "id пользователя должен быть больше 0") int userId) {
        reviewService.addLikeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeReview(@PathVariable @Positive(message = "id отзыва должен быть больше 0") int id,
                                 @PathVariable @Positive(message = "id пользователя должен быть больше 0") int userId) {
        reviewService.addDislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeReview(@PathVariable @Positive(message = "id отзыва должен быть больше 0") int id,
                              @PathVariable @Positive(message = "id пользователя должен быть больше 0") int userId) {
        reviewService.delLikeReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable @Positive(message = "id отзыва должен быть больше 0") int id,
                                 @PathVariable @Positive(message = "id пользователя должен быть больше 0") int userId) {
        reviewService.delDislikeReview(id, userId);
    }
}
