package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getRecommendations(int id) {
        String sql = "SELECT DISTINCT F.ID, F.NAME, DESCRIPTION," +
                "                     RELEASE_DATE, DURATION, " +
                "                     RATING_MPA, M.NAME MPA_NAME " + //уменьшаем количество обращений для получения mpa
                " FROM FILMS F" +
                "      JOIN FILMS_LIKE FL on F.ID = FL.FILM_ID " +
                "      LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                "           WHERE FL.USER_ID IN (" +//users с общими фильмами
                "                     SELECT DISTINCT F2.USER_ID AS USER_COMMON_FILMS" +
                "                            FROM FILMS_LIKE F" +
                "                                 JOIN FILMS_LIKE FL on F.FILM_ID = FL.FILM_ID" +
                "                                 JOIN FILMS_LIKE F2 on F2.FILM_ID = FL.FILM_ID" +
                "                               WHERE FL.USER_ID = ?" +
                "                               )" +
                "                  AND F.ID NOT IN (" +//фильмы, которых нет у юзера n
                "                      SELECT FILM_ID FROM FILMS_LIKE WHERE USER_ID = ?" +
                "                                   )";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), id, id);
    }

    private Film makeObjectFilm(ResultSet resultSet) throws SQLException {
        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getTimestamp("release_date").toLocalDateTime().toLocalDate())
                .duration(resultSet.getLong("duration"))
                .mpa(new Mpa(resultSet.getInt("rating_mpa"), resultSet.getString("mpa_name")))
                .usersLikes(new ArrayList<>())
                .genres(new LinkedList<>())
                .directors(new ArrayList<>())
                .build();
    }

    @Override
    public void loadFilmsLikes(List<Film> films) {
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbcTemplate.query(
                String.format("select FILM_ID, USER_ID from FILMS_LIKE " +
                        "      where FILM_ID in (%s)", inSql),
                ids.toArray(),
                (rs, rowNum) -> makeFilmsListWithLikes(rs, films));
    }

    private Film makeFilmsListWithLikes(ResultSet rs, List<Film> films) throws SQLException {
        int filmId = rs.getInt("film_id");
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        filmMap.get(filmId).addLikes(rs.getInt("user_id"));

        return filmMap.get(filmId);
    }

    @Override
    public List<Film> findFilms() {
        return jdbcTemplate.query(
                "SELECT  F.ID, F.NAME, DESCRIPTION, " +
                        "    RELEASE_DATE, DURATION, " +
                        "    RATING_MPA, M.NAME MPA_NAME  " + //уменьшаем количество обращений для получения mpa
                        "FROM FILMS F " +
                        "     LEFT JOIN MPA M on M.ID = F.RATING_MPA ",
                (rs, rowNum) -> makeObjectFilm(rs));
    }

    @Override
    public List<Film> findCommonFilms(int userId, int friendId) {
        String sqlQuery = "SELECT DISTINCT F.id, F.name, " +
                "description, " +
                "release_date, " +
                "duration, " +
                "rating_mpa, " +
                "M.NAME MPA_NAME " +
                "FROM FILMS F " +
                "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                "INNER JOIN films_like fl ON f.id = fl.film_id " +
                "INNER JOIN films_like f2 ON f2.film_id = fl.film_id " +
                "WHERE fl.user_id = ? AND f2.user_id = ?";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeObjectFilm(rs));
    }

    @Override
    public List<Film> findPopular(Integer limit, String condition) {
        String sql = "SELECT f.id, " +
                "F.name, " +
                "description, " +
                "release_date, " +
                "duration, " +
                "f.rating_mpa, " +
                "m.name mpa_name " +
                "FROM FILMS F " +
                "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                "LEFT JOIN films_genre AS fg ON f.id = fg.film_id " +
                "LEFT JOIN films_like AS fl ON f.id = fl.film_id " +
                condition +
                "GROUP BY f.id, f.name,f.description, f.release_date, f.duration, f.rating_mpa " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), limit);
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        String sqlQuery = "SELECT * FROM films WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, filmId));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public int createFilm(Film film) {
        KeyHolder generatedId = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, rating_mpa) VALUES(?,?,?,?,?)",
                    new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, generatedId);
        film.setId(Objects.requireNonNull(generatedId.getKey()).intValue());

        if (film.getGenres() != null) {
            addGenresToFilm(film);
        }
        if (film.getDirectors() != null) {
            addDirectorsToFilm(film);
        }
        film.setMpa(getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));
        film.setDirectors(getDirectorsByFilmId(film.getId()));
        log.info("Film with id: {} created", film.getId());
        return film.getId();
    }

    @Override
    public void updateFilm(Film film) {
        jdbcTemplate.update(
                "DELETE FROM films_genre WHERE film_id = ?",
                film.getId());

        if (film.getGenres() != null) {
            addGenresToFilm(film);
        }
        jdbcTemplate.update(
                "DELETE FROM films_director WHERE film_id = ?",
                film.getId());
        if (film.getDirectors() != null) {
            addDirectorsToFilm(film);
        }
        int row = jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_mpa = ?" +
                        "WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        if (row != 1) {
            throw new NotExistException("Film with id: " + film.getId() + " does not exist");
        }
        film.setMpa(getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));
        film.setDirectors(getDirectorsByFilmId(film.getId()));
        log.info("Film with id: {} edited", film.getId());
    }

    @Override
    public void deleteFilmById(int filmId) {
        jdbcTemplate.update(
                "DELETE FROM films WHERE id = ?",
                filmId);
        log.info("Film with id: {} deleted", filmId);
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update(
                "MERGE INTO films_like (film_id, user_id) VALUES (?, ?)",
                filmId,
                userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM films_like WHERE film_id = ? AND user_id = ?",
                filmId,
                userId);
        getFilmById(filmId);
    }

    @Override
    public List<Film> findFilmsByDirectorIdSortedByLike(int directorId) {
        String sql = "SELECT f.* ," +
                " m.name mpa_name " +
                "FROM FILMS F " +
                "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                "LEFT JOIN films_like as fl ON fl.film_id = f.id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.ID " +
                "ORDER BY count(fl.USER_ID) DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), directorId);
    }

    @Override
    public List<Film> findFilmsByDirectorIdSortedByReleaseDate(int directorId) {
        String sql = "SELECT F.*, m.name mpa_name " +
                "FROM FILMS F " +
                "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY f.release_date";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), directorId);
    }

    @Override
    public boolean checkFilmExist(int filmId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM films WHERE id = ?",
                filmId);
        return userRows.next();
    }

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        String likeValue = "%" + query.toLowerCase() + "%";
        String sql =
                "SELECT F.*, M.NAME MPA_NAME " +
                        "FROM FILMS F " +
                        "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                        "LEFT JOIN films_like as l on l.film_id = f.id " +
                        "WHERE LOWER(f.name) like ? " +
                        "GROUP BY f.id " +
                        "ORDER BY COUNT(l.film_id) DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), likeValue);
    }

    @Override
    public List<Film> searchFilmsByDirector(String query) {
        String likeValue = "%" + query.toLowerCase() + "%";
        String sql = "SELECT DISTINCT f.*, COUNT(l.film_id), M.NAME MPA_NAME " +
                "FROM FILMS F " +
                "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                "LEFT JOIN director as d ON d.id = fd.director_id " +
                "LEFT JOIN films_like as l on l.film_id = f.id " +
                "WHERE LOWER(d.name) like ? " +
                "GROUP BY f.id ORDER BY COUNT(l.film_id) DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), likeValue);
    }

    @Override
    public List<Film> searchFilmsByTitleAndDirector(String query) {
        String likeValue = "%" + query.toLowerCase() + "%";
        String sql =
                "SELECT DISTINCT f.*, COUNT(l.film_id), M.NAME MPA_NAME " +
                        "FROM FILMS F " +
                        "LEFT JOIN MPA M on M.ID = F.RATING_MPA " +
                        "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                        "LEFT JOIN director as d ON d.id = fd.director_id " +
                        "LEFT JOIN films_like as l on l.film_id = f.id " +
                        "WHERE LOWER(d.name) like ? OR LOWER(f.name) like ?" +
                        "GROUP BY f.id ORDER BY COUNT(l.film_id) DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), likeValue, likeValue);
    }

    private void addGenresToFilm(Film film) {
        for (Genre genre : film.getGenres()) {
            SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                    "SELECT * FROM films_genre WHERE film_id = ? AND genre_id = ?",
                    film.getId(),
                    genre.getId());
            // Проверка, что такой жанр еще не добавлен
            if (!genreRows.next()) {
                jdbcTemplate.update(
                        "INSERT INTO films_genre (film_id, genre_id) VALUES (?,?)",
                        film.getId(),
                        genre.getId());
            }
        }
    }

    private void addDirectorsToFilm(Film film) {
        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(
                    "MERGE INTO films_director (film_id, director_id) VALUES (?,?)",
                    film.getId(),
                    director.getId());
        }
    }

    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        final String description = resultSet.getString("description");
        final LocalDate releaseDate = resultSet.getDate("release_date").toLocalDate();
        long duration = resultSet.getLong("duration");
        int mpaId = resultSet.getInt("rating_mpa");
        return new Film(id, name, description, releaseDate, duration, getLikesByFilmId(id), getMpaById(mpaId),
                getGenresByFilmId(id), getDirectorsByFilmId(id));
    }

    @Override
    public List<Integer> getLikesByFilmId(int filmId) {
        return jdbcTemplate.queryForList(
                "SELECT user_id FROM films_like WHERE film_id = ?",
                Integer.class,
                filmId);
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.id, g.name FROM genre as g " +
                        "LEFT JOIN films_genre as fg on g.id = fg.genre_id " +
                        "WHERE film_id = ?",
                this::makeGenre,
                filmId);
    }

    private Mpa getMpaById(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM mpa WHERE id = ?",
                this::makeMpa,
                id);
    }

    private List<Director> getDirectorsByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT d.* FROM director as d " +
                        "LEFT JOIN films_director as fd ON fd.director_id = d.id " +
                        "WHERE fd.film_id = ?",
                this::makeDirector,
                filmId);
    }

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        return new Genre(id, name);
    }

    private Mpa makeMpa(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        return new Mpa(id, name);
    }

    private Director makeDirector(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        return new Director(id, name);
    }
}