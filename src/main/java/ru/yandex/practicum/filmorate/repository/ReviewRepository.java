package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.EntityUpdateErrorException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewRepository extends BaseRepository<Review> {

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<Review> getAllReviews(int count) {
        String query = "select * from reviews order by useful desc, review_id asc limit ?";
        return getRecords(query, count);
    }

    public List<Review> getAllReviewsByFilmId(int filmId, int count) {
        String query = "select * from reviews where film_id = ? order by useful desc, review_id asc limit ?";
        return getRecords(query, filmId, count);
    }

    public Optional<Review> getReviewById(int id) {
        String query = "select * from reviews where review_id = ?";
        return getRecord(query, id);
    }

    public Review addReview(Review review) {
        String query = "insert into reviews (content, is_positive, user_id, film_id, useful) " +
                       "values (?, ?, ?, ?, ?)";
        int id = insert(query,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        ).intValue();
        review.setReviewId(id);
        return review;
    }

    public Review updateReview(Review review) {
        String query = "update reviews set " +
                       "content = ?, " +
                       "is_positive = ? " +
                       "where review_id = ?";
        int result = update(query,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        if (result == 0) {
            throw new EntityUpdateErrorException("Не удалось обновить отзыв");
        }
        return getReviewById(review.getReviewId()).orElseThrow();
    }

    public void deleteReview(int id) {
        String delEvent = "delete from events where type = 'REVIEW' and entity_id = ?";
        delete(delEvent, id);

        String delLikes = "delete from review_likes where review_id = ?";
        delete(delLikes, id);

        String delReview = "delete from reviews where review_id = ?";
        delete(delReview, id);
    }

    public void addLikeReview(int reviewId, int userId) {
        String checkExistsQuery = "select is_positive from review_likes where review_id = ? and user_id = ?";
        try {
            Boolean currentReaction = jdbc.queryForObject(checkExistsQuery, Boolean.class, reviewId, userId);

            if (!currentReaction) {
                String updateReactionQuery = "update review_likes set is_positive = true where review_id = ? and user_id = ?";
                update(updateReactionQuery, reviewId, userId);
                String updateUsefulQuery = "update reviews set useful = useful + 2 where review_id = ?";
                update(updateUsefulQuery, reviewId);
            }
        } catch (EmptyResultDataAccessException e) {
            String insertQuery = "insert into review_likes (review_id, user_id, is_positive) values (?, ?, true)";
            update(insertQuery, reviewId, userId);
            String updateUsefulQuery = "update reviews set useful = useful + 1 where review_id = ?";
            update(updateUsefulQuery, reviewId);
        }
    }

    public void delLikeReview(int reviewId, int userId) {
        String checkExistsQuery = "select is_positive from review_likes where review_id = ? and user_id = ?";
        try {
            Boolean currentReaction = jdbc.queryForObject(checkExistsQuery, Boolean.class, reviewId, userId);

            if (currentReaction) {
                String deleteReactionQuery = "delete from review_likes where review_id = ? and user_id = ?";
                update(deleteReactionQuery, reviewId, userId);
                String updateUsefulQuery = "update reviews set useful = useful - 1 where review_id = ?";
                update(updateUsefulQuery, reviewId);
            }
        } catch (EmptyResultDataAccessException e) {
            log.debug("Попытка удаления лайка: реакция пользователя {} на отзыв {} не найдена.", userId, reviewId);
        }
    }

    public void addDislikeReview(int reviewId, int userId) {
        String checkExistsQuery = "select is_positive from review_likes where review_id = ? and user_id = ?";
        try {
            Boolean currentReaction = jdbc.queryForObject(checkExistsQuery, Boolean.class, reviewId, userId);

            if (currentReaction) {
                String updateReactionQuery = "update review_likes set is_positive = false where review_id = ? and user_id = ?";
                update(updateReactionQuery, reviewId, userId);
                String updateUsefulQuery = "update reviews set useful = useful - 2 where review_id = ?";
                update(updateUsefulQuery, reviewId);
            }
        } catch (EmptyResultDataAccessException e) {
            String insertQuery = "insert into review_likes (review_id, user_id, is_positive) values (?, ?, false)";
            update(insertQuery, reviewId, userId);
            String updateUsefulQuery = "update reviews set useful = useful - 1 where review_id = ?";
            update(updateUsefulQuery, reviewId);
        }
    }

    public void delDislikeReview(int reviewId, int userId) {
        String checkExistsQuery = "select is_positive from review_likes where review_id = ? and user_id = ?";
        try {
            Boolean currentReaction = jdbc.queryForObject(checkExistsQuery, Boolean.class, reviewId, userId);

            if (!currentReaction) {
                String deleteReactionQuery = "delete from review_likes where review_id = ? and user_id = ?";
                update(deleteReactionQuery, reviewId, userId);
                String updateUsefulQuery = "update reviews set useful = useful + 1 where review_id = ?";
                update(updateUsefulQuery, reviewId);
            }
        } catch (EmptyResultDataAccessException e) {
            String insertQuery = "insert into review_likes (review_id, user_id, is_positive) values (?, ?, false)";
            update(insertQuery, reviewId, userId);
            String updateUsefulQuery = "update reviews set useful = useful - 1 where review_id = ?";
            update(updateUsefulQuery, reviewId);
        }
    }

    public void updateReviewReaction(int reviewId, int userId, boolean isPositive) {
        String sql = "update review_likes set is_positive = ? where review_id = ? and user_id = ?";
        int result = update(sql, isPositive, reviewId, userId);

        if (result == 0) {
            throw new EntityUpdateErrorException("Не удалось обновить реакцию пользователя");
        }

        String updateUseful;
        if (isPositive) {
            updateUseful = "update reviews set useful = useful + 2 where review_id = ?";
        } else {
            updateUseful = "update reviews set useful = useful - 2 where review_id = ?";
        }
        update(updateUseful, reviewId);
    }

    public Optional<Boolean> findUserReviewReaction(int reviewId, int userId) {
        String sql = "select is_positive from review_likes where review_id = ? and user_id = ?";
        try {
            Boolean result = jdbc.queryForObject(sql, Boolean.class, reviewId, userId);
            return Optional.of(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}