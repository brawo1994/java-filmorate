package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.EventHistory;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event_history.EventHistoryStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;
    private final UserService userService;
    private final EventHistoryStorage eventHistoryStorage;

    public List<Review> getReviews(Map<String, Object> filters, int limit) {
        log.info("Start getting reviews");

        List<Review> reviews = reviewStorage.getReviews(filters, limit);

        log.info("Finish getting reviews");

        return reviews;
    }

    public Review getReviewById(int id) {
        log.info("Start getting review by id: {}", id);

        throwIfReviewNotExist(id);

        Review review = reviewStorage.getReviewById(id);

        log.info("Finish getting review by id: {}", id);

        return review;
    }

    public Review createReview(Review review) {
        log.info("Start creating a review: {}", review);

        int filmId = review.getFilmId();
        filmService.throwIfFilmNotExist(filmId);

        int userId = review.getUserId();
        userService.throwIfUserNotExist(List.of(userId));

        int reviewId = reviewStorage.createReview(review);
        Review createdReview = reviewStorage.getReviewById(reviewId);
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(userId)
                .eventType(EventType.REVIEW)
                .operation(OperationType.ADD)
                .entityId(reviewId)
                .build());

        log.info("Finish creating a review: {}", createdReview);

        return createdReview;
    }

    public Review updateReview(Review review) {
        log.info("Start updating a review: {}", review);

        throwIfReviewNotExist(review.getReviewId());
        filmService.throwIfFilmNotExist(review.getFilmId());
        userService.throwIfUserNotExist(List.of(review.getUserId()));

        reviewStorage.updateReview(review);
        Review updatedReview = getReviewById(review.getReviewId());
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(updatedReview.getUserId())
                .eventType(EventType.REVIEW)
                .operation(OperationType.UPDATE)
                .entityId(updatedReview.getFilmId())
                .build());

        log.info("Finish updating a review: {}", updatedReview);

        return updatedReview;
    }

    public Review addLike(int reviewId, int userId) {
        log.info("Start adding like from user with id: {} to review with id: {}", userId, reviewId);

        throwIfReviewNotExist(reviewId);
        userService.throwIfUserNotExist(List.of(userId));

        reviewStorage.addReviewGrade(reviewId, userId, 1);
        Review review = getReviewById(reviewId);

        log.info("Finish adding like from user with id: {} to review with id: {}", userId, reviewId);

        return review;
    }

    public Review addDislike(int reviewId, int userId) {
        log.info("Start adding dislike from user with id: {} to review with id: {}", userId, reviewId);

        throwIfReviewNotExist(reviewId);
        userService.throwIfUserNotExist(List.of(userId));

        reviewStorage.addReviewGrade(reviewId, userId, -1);
        Review review = getReviewById(reviewId);

        log.info("Finish adding dislike from user with id: {} to review with id: {}", userId, reviewId);

        return review;
    }

    public void deleteReview(int reviewId) {
        log.info("Start deleting review with id: {}", reviewId);

        throwIfReviewNotExist(reviewId);
        Review review = getReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(OperationType.REMOVE)
                .entityId(review.getReviewId())
                .build());

        log.info("Finish deleting review with id: {}", reviewId);
    }

    private void throwIfReviewNotExist(int reviewId) {
        if (reviewStorage.isReviewNotExist(reviewId)) {
            throw new NotExistException("Review with id: " + reviewId + " does not exist");
        }
    }

}
