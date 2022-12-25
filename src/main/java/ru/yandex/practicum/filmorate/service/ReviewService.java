package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Review> getReviews(Map<String, Object> filters, int limit) {
        log.info("Start getting reviews");

        Collection<Review> reviews = reviewStorage.getReviews(filters, limit);

        log.info("Finish getting reviews");

        return reviews;
    }

    public Review getReviewById(int id) {
        log.info("Start getting review by id: {}", id);

        if (reviewStorage.isReviewNotExist(id)) {
            throw new NotExistException("Review with id: " + id + " does not exist");
        }

        Review review = reviewStorage.getReviewById(id);

        log.info("Finish getting review by id: {}", id);

        return review;
    }

    public Review createReview(Review review) {
        log.info("Start creating a review: {}", review);

        int filmId = review.getFilmId();
        if (!filmStorage.checkFilmExist(filmId)) {
            throw new NotExistException("Film with id: " + filmId + " does not exist");
        }
        int userId = review.getUserId();
        if (!userStorage.checkUserExist(userId)) {
            throw new NotExistException("User with id: " + userId + " does not exist");
        }

        int reviewId = reviewStorage.createReview(review);
        Review createdReview = reviewStorage.getReviewById(reviewId);

        log.info("Finish creating a review: {}", createdReview);

        return createdReview;
    }

    public Review updateReview(Review review) {
        log.info("Start updating a review: {}", review);

        checkReviewExist(review.getReviewId());

        reviewStorage.updateReview(review);
        Review updatedReview = getReviewById(review.getReviewId());

        log.info("Finish updating a review: {}", updatedReview);

        return updatedReview;
    }

    public Review addReviewGrade(int reviewId, int userId, int grade) {
        log.info("Start adding like from user with id: {} to review with id: {}", userId, reviewId);

        checkReviewExist(reviewId);
        if (!userStorage.checkUserExist(userId)) {
            throw new NotExistException("User with id: " + userId + " does not exist");
        }

        reviewStorage.addReviewGrade(reviewId, userId, grade);
        Review review = getReviewById(reviewId);

        log.info("Finish adding like from user with id: {} to review with id: {}", userId, reviewId);

        return review;
    }

    public void deleteReview(int reviewId) {
        log.info("Start deleting review with id: {}", reviewId);

        reviewStorage.deleteReview(reviewId);

        log.info("Finish deleting review with id: {}", reviewId);
    }

    private void checkReviewExist(int reviewId) {
        if (reviewStorage.isReviewNotExist(reviewId)) {
            throw new NotExistException("Review with id: " + reviewId + " does not exist");
        }
    }

}
