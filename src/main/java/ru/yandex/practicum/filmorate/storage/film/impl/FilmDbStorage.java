package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> getFilms() {
        return jdbcTemplate.query(
                "SELECT * FROM films",
                this::makeFilm);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "SELECT DISTINCT f.id, " +
                                          "f.name, " +
                                          "f.description, " +
                                          "f.release_date, " +
                                          "f.duration, " +
                                          "f.rating_mpa " +
                          "FROM films f " +
                          "INNER JOIN films_like fl ON f.id = fl.film_id " +
                          "INNER JOIN films_like f2 ON f2.film_id = fl.film_id " +
                          "WHERE fl.user_id = ? AND f2.user_id = ?";

        return jdbcTemplate.query(sqlQuery, this::makeFilm, userId, friendId);
    }

    @Override
    public Film getFilmById(int filmId) {
        if (!checkFilmExist(filmId))
            throw new NotExistException("Film with id: " + filmId + " does not exist");
        return jdbcTemplate.queryForObject(
                "SELECT * FROM films WHERE id = ?",
                this::makeFilm,
                filmId);
    }

    @Override
    public Film createFilm(Film film) {
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
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
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
        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_mpa = ?" +
                        "WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        film.setMpa(getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));
        film.setDirectors(getDirectorsByFilmId(film.getId()));
        log.info("Film with id: {} edited", film.getId());
        return film;
    }

    @Override
    public Film deleteFilmById(int filmId) {
        Film film = getFilmById(filmId);
        jdbcTemplate.update(
                "DELETE FROM films WHERE id = ?",
                filmId);
        log.info("Film with id: {} deleted", filmId);
        return film;
    }

    @Override
    public Film addLike(int filmId, int userId) {
        jdbcTemplate.update(
                "INSERT INTO films_like (film_id, user_id) VALUES (?, ?)",
                filmId,
                userId);
        return getFilmById(filmId);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM films_like WHERE film_id = ? AND user_id = ?",
                filmId,
                userId);
        return getFilmById(filmId);
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortedByLike(int directorId) {
        return jdbcTemplate.query(
                "SELECT f.* FROM films as f " +
                        "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                        "LEFT JOIN films_like as fl ON fl.film_id = f.id " +
                        "WHERE fd.director_id = ? " +
                        "GROUP BY f.ID " +
                        "ORDER BY count(fl.*) DESC",
                this::makeFilm,
                directorId);
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortedByReleaseDate(int directorId) {
        return jdbcTemplate.query(
                "SELECT f.* FROM films as f " +
                        "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                        "WHERE fd.director_id = ? " +
                        "ORDER BY f.release_date",
                this::makeFilm,
                directorId);
    }

    @Override
    public boolean checkFilmExist(int filmId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM films WHERE id = ?",
                filmId);
        return userRows.next();
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
        return new Film(id, name, description, releaseDate, duration, getLikesByFilmId(id), getMpaById(mpaId), getGenresByFilmId(id), getDirectorsByFilmId(id));
    }

    private List<Integer> getLikesByFilmId(int filmId) {
        return jdbcTemplate.queryForList(
                "SELECT user_id FROM films_like WHERE film_id = ?",
                Integer.class,
                filmId);
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.id, g.name FROM genre as g LEFT JOIN films_genre as fg on g.id = fg.genre_id WHERE film_id = ?",
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
                "SELECT d.* FROM director as d LEFT JOIN films_director as fd ON fd.director_id = d.id WHERE fd.film_id = ?",
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
