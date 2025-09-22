package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.api.dto.CreateReviewRequest;
import ru.yandex.practicum.filmorate.api.dto.ReviewDTO;
import ru.yandex.practicum.filmorate.api.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.api.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         FilmRepository filmRepository,
                         EventService eventService
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.eventService = eventService;

    }

    public List<ReviewDTO> getReviews(int filmId,  int count) {
        log.info("Запрос на получения всех отзывов: filmId={}, count={}", filmId, count);

        List<ReviewDTO> dtoList;
        try {
            if (filmId == 0) {
                dtoList = reviewRepository.getAllReviews(count)
                        .stream()
                        .map(ReviewMapper::mapToReviewDto)
                        .toList();
            } else {
                dtoList = reviewRepository.getAllReviewsByFilmId(filmId, count)
                        .stream()
                        .map(ReviewMapper::mapToReviewDto)
                        .toList();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении отзывов: filmId={}, count={}", filmId, count, e);
            throw new RuntimeException("Ошибка при получении отзывов", e);
        }

        if (dtoList.isEmpty()) {
            log.info("Отзывы не найдены");
        } else {
            log.info("Отзывы найдены в количестве {}", dtoList.size());
        }

        return dtoList;
    }

    public ReviewDTO getReview(int id) {
        log.info("Запрос на получения отзыва с id = {}", id);

        Review review = reviewRepository.getReviewById(id)
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        log.info("Отзыв с id = {} успешно найден", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDTO addReview(CreateReviewRequest reviewRequest) {
        log.info("Получен запрос на создание отзыва от пользователя с id = {} к фильму с id = {}",
                reviewRequest.getUserId(), reviewRequest.getFilmId());

        if (reviewRequest.getFilmId() == 0 || reviewRequest.getUserId() == 0) {
            throw new ValidationException("Id фильма и пользователя не может быть null");
        }

        userRepository.getUserById(reviewRequest.getUserId())
                .orElseThrow(() -> new NotFoundIssueException("Пользователь не найден"));

        filmRepository.getFilmById(reviewRequest.getFilmId())
                .orElseThrow(() -> new NotFoundIssueException("Фильм не найден"));

        Review review = reviewRepository.addReview(ReviewMapper.mapToReview(reviewRequest));

        log.info("Отзыв с id = {} успешно создан", review.getReviewId());

        eventService.createEvent(Event.builder()
                .user(userRepository.getUserById(reviewRequest.getUserId()).orElseThrow())
                .entityId(review.getReviewId())
                .type(EventType.REVIEW)
                .operation(EventOperation.ADD)
                .timestamp(Instant.now())
                .build()
        );

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDTO updateReview(UpdateReviewRequest reviewRequest) {
        log.info("Получен запрос на обновление отзыва с id = {}", reviewRequest.getReviewId());

        if (reviewRequest.getFilmId() == 0 || reviewRequest.getUserId() == 0) {
            throw new ValidationException("Id фильма и пользователя не может быть null");
        }

        reviewRepository.getReviewById(reviewRequest.getReviewId())
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        userRepository.getUserById(reviewRequest.getUserId())
                .orElseThrow(() -> new NotFoundIssueException("Пользователь не найден"));

        filmRepository.getFilmById(reviewRequest.getFilmId())
                .orElseThrow(() -> new NotFoundIssueException("Фильм не найден"));

        Review review = reviewRepository.updateReview(ReviewMapper.mapToReview(reviewRequest));

        log.info("Отзыв с id = {} успешно обновлен", review.getReviewId());

        eventService.createEvent(Event.builder()
                .user(userRepository.getUserById(reviewRequest.getUserId()).orElseThrow())
                .entityId(review.getReviewId())
                .type(EventType.REVIEW)
                .operation(EventOperation.UPDATE)
                .timestamp(Instant.now())
                .build()
        );

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDTO deleteReview(int id) {
        log.info("Запрос на удаление отзыва с id = {}", id);

        Review review = reviewRepository.getReviewById(id)
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        reviewRepository.deleteReview(id);

        log.info("Фильм с id = {} успешно удален", review.getReviewId());

        eventService.createEvent(Event.builder()
                .user(userRepository.getUserById(review.getUserId()).orElseThrow())
                .entityId(review.getReviewId())
                .type(EventType.REVIEW)
                .operation(EventOperation.REMOVE)
                .timestamp(Instant.now())
                .build()
        );

        return ReviewMapper.mapToReviewDto(review);
    }

    public void addLikeReview(int reviewId, int userId) {
        log.info("Запрос на добавление лайка отзыву id = {} от пользователя id = {}", reviewId, userId);

        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundIssueException("Пользователь не найден"));

        Optional<Boolean> reaction = reviewRepository.findUserReviewReaction(reviewId, userId);
        if (reaction.isEmpty()) {
            reviewRepository.addLikeReview(reviewId, userId);
            log.info("Лайк успешно добавлен (пользователь id = {}, отзыв id = {})", userId, reviewId);
        } else if (reaction.orElse(false)) {
            reviewRepository.updateReviewReaction(reviewId, userId, true);
            log.info("Дизлайк заменён на лайк (пользователь id = {}, отзыв id = {})", userId, reviewId);
        } else {
            log.warn("Нельзя поставить лайк дважды (пользователь id = {}, отзыв id = {})", userId, reviewId);
            throw new ValidationException("Нельзя поставить лайк дважды");
        }
    }

    public void addDislikeReview(int reviewId, int userId) {
        log.info("Запрос на добавление дизлайка отзыву id = {} от пользователя id = {}", reviewId, userId);

        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundIssueException("Пользователь не найден"));

        Optional<Boolean> reaction = reviewRepository.findUserReviewReaction(reviewId, userId);
        if (reaction.isEmpty()) {
            reviewRepository.addDislikeReview(reviewId, userId);
            log.info("Дизлайк успешно добавлен (пользователь id = {}, отзыв id = {})", userId, reviewId);
        } else if (reaction.orElse(true)) {
            reviewRepository.updateReviewReaction(reviewId, userId, false);
            log.info("Лайк заменён на дизлайк (пользователь id = {}, отзыв id = {})", userId, reviewId);
        } else {
            log.warn("Нельзя поставить дизлайк дважды (пользователь id = {}, отзыв id = {})", userId, reviewId);
            throw new ValidationException("Нельзя поставить дизлайк дважды");
        }
    }

    public void delLikeReview(int reviewId, int userId) {
        log.info("Запрос на удаление лайка отзыву с id = {} от пользователя с id = {}", reviewId, userId);

        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundIssueException("Пользователь не найден"));

        reviewRepository.delLikeReview(reviewId, userId);

        log.info("Лайк от пользователя с id = {} к отзыву с id = {} успешно удален", userId, reviewId);
    }

    public void delDislikeReview(int reviewId, int userId) {
        log.info("Запрос на удаление дизлайка отзыву с id = {} от пользователя с id = {}", reviewId, userId);

        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundIssueException("Отзыв не найден"));

        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundIssueException("Пользователь не найден"));

        reviewRepository.delDislikeReview(reviewId, userId);

        log.info("Дизлайк от пользователя с id = {} к отзыву с id = {} успешно удален", userId, reviewId);
    }
}