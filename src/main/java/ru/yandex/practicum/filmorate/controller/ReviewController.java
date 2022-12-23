package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public Collection<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                         @RequestParam(defaultValue = "10") int count) {

        Map<String, Object> filters = new HashMap<>();
        if (filmId != null) {
            filters.put("FILM_ID", filmId);
        }

        return reviewService.getReviews(filters, count);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable int reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Review addLike(@PathVariable int reviewId, @PathVariable int userId) {
        // Т.к. мы добавляем лайк, то оценка отзыва пользователем равна 1.
        return reviewService.addReviewGrade(reviewId, userId, 1);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Review addDislike(@PathVariable int reviewId, @PathVariable int userId) {
        // Т.к. мы добавляем дизлайк, то оценка отзыва пользователем равна -1.
        return reviewService.addReviewGrade(reviewId, userId, -1);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable int reviewId) {
        reviewService.deleteReview(reviewId);
    }

}
