package ru.yandex.practicum.filmorate.storage.review.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Review> getReviews(Map<String, Object> filters, int limit) {
        StringBuilder builderQuery = new StringBuilder("SELECT REVIEW.REVIEW_ID,\n" +
                "    REVIEW.FILM_ID,\n" +
                "    REVIEW.USER_ID,\n" +
                "    REVIEW.CONTENT,\n" +
                "    REVIEW.IS_POSITIVE,\n" +
                "    SUM(COALESCE(R_LIKE.GRADE, 0)) AS USEFUL\n" +
                "FROM FILMS_REVIEW AS REVIEW\n" +
                "LEFT JOIN REVIEWS_LIKE AS R_LIKE ON REVIEW.REVIEW_ID = R_LIKE.REVIEW_ID");
       List<Object> params = new ArrayList<>();

        // Add filters and params.
        if (!filters.isEmpty()) {
            builderQuery.append(" WHERE ");
            int i = 0;
            for (Map.Entry<String, Object> filter: filters.entrySet()) {
                if (i++ > 0) {
                    builderQuery.append(", ");
                }

                builderQuery.append(filter.getKey()).append(" = ?");
                params.add(filter.getValue());
            }
        }

        // Add sort and limit.
        builderQuery.append(" GROUP BY REVIEW.REVIEW_ID\n" +
                "ORDER BY USEFUL DESC\n" +
                "LIMIT ").append(limit);

        String query = builderQuery.toString();
        return jdbcTemplate.query(query, this::makeReview, params.toArray());
    }

    @Override
    public Review getReviewById(int reviewId) {
        final String QUERY = "SELECT REVIEW.REVIEW_ID,\n" +
                "       REVIEW.FILM_ID,\n" +
                "       REVIEW.USER_ID,\n" +
                "       REVIEW.CONTENT,\n" +
                "       REVIEW.IS_POSITIVE,\n" +
                "       SUM(COALESCE(R_LIKE.GRADE, 0)) AS USEFUL\n" +
                "FROM FILMS_REVIEW AS REVIEW\n" +
                "         LEFT JOIN REVIEWS_LIKE AS R_LIKE ON REVIEW.REVIEW_ID = R_LIKE.REVIEW_ID\n" +
                "WHERE REVIEW.REVIEW_ID = ?\n" +
                "GROUP BY REVIEW.REVIEW_ID";
        return jdbcTemplate.queryForObject(QUERY, this::makeReview, reviewId);
    }

    @Override
    public int createReview(Review review) {
        // Create review in db films_review.
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).
                withTableName("FILMS_REVIEW").
                usingGeneratedKeyColumns("REVIEW_ID").
                usingColumns("FILM_ID", "USER_ID", "CONTENT", "IS_POSITIVE");

        Map<String, Object> params = new HashMap<>();
        params.put("FILM_ID", review.getFilmId());
        params.put("USER_ID", review.getUserId());
        params.put("CONTENT", review.getContent());
        params.put("IS_POSITIVE", review.getIsPositive());

        return simpleJdbcInsert.executeAndReturnKey(params).intValue();
    }

    @Override
    public void updateReview(Review review) {
        final String UPDATE_REQUEST = "UPDATE FILMS_REVIEW SET " +
                "CONTENT = ?, IS_POSITIVE = ?" +
                "WHERE REVIEW_ID = ?";
        jdbcTemplate.update(UPDATE_REQUEST,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
    }

    @Override
    public void addReviewGrade(int reviewId, int userId, int grade) {
        final String SQL_QUERY = "INSERT INTO REVIEWS_LIKE (REVIEW_ID, USER_ID, GRADE) VALUES (?, ?, ?)";
        jdbcTemplate.update(SQL_QUERY, reviewId, userId, grade);
    }

    @Override
    public void deleteReview(int reviewId) {
        final String FILMS_REVIEW_DELETE = "DELETE FROM FILMS_REVIEW WHERE REVIEW_ID = ?";
        jdbcTemplate.update(FILMS_REVIEW_DELETE, reviewId);
    }

    @Override
    public boolean isReviewNotExist(int reviewId) {
        final String SQL_QUERY = "SELECT REVIEW_ID\n" +
                "FROM FILMS_REVIEW\n" +
                "WHERE REVIEW_ID = ?";
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(SQL_QUERY, reviewId);

        return !reviewRows.next();
    }

    private Review makeReview(ResultSet resultSet, int rowNum) throws SQLException {
        int reviewId = resultSet.getInt("REVIEW_ID");
        int filmId = resultSet.getInt("FILM_ID");
        int userId = resultSet.getInt("USER_ID");
        String content = resultSet.getString("CONTENT");
        Boolean isPositive = resultSet.getBoolean("IS_POSITIVE");
        int useful = resultSet.getInt("USEFUL");

        return new Review(reviewId, filmId, userId, content, isPositive, useful);
    }

}
