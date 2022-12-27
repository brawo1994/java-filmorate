package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Map;

public interface ReviewStorage {

    List<Review> getReviews(Map<String, Object> filters, int limit);

    Review getReviewById(int reviewId);

    int createReview(Review review);

    void updateReview(Review review);

    void addReviewGrade(int reviewId, int userId, int grade);

    void deleteReview(int reviewId);

    boolean isReviewNotExist(int reviewId);
}
