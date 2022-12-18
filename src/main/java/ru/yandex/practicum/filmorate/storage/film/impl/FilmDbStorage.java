package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return jdbcTemplate.query(
                "SELECT * FROM films",
                this::makeFilm);
    }

    @Override
    public Film getFilmById(int filmId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM films WHERE id = ?",
                filmId);
        if (!userRows.next())
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

        if (film.getGenres() != null){
            addGenresToFilm(film);
        }
        film.setMpa(getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));
        log.info("Film with id: {} created", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        if (film.getGenres() != null) {
            jdbcTemplate.update(
                    "DELETE FROM films_genre WHERE film_id = ?",
                    film.getId());
            addGenresToFilm(film);
        }

        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_mpa = ? WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        film.setMpa(getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));
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

    private void addGenresToFilm(Film film){
        for (Genre genre : film.getGenres()) {
            SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                    "SELECT * FROM films_genre WHERE film_id = ? AND genre_id = ?",
                    film.getId(),
                    genre.getId());
            // Проверка, что такой жанр еще не добавлен
            if (!genreRows.next()){
                jdbcTemplate.update(
                        "INSERT INTO films_genre (film_id, genre_id) VALUES (?,?)",
                        film.getId(),
                        genre.getId());
            }
        }
    }

    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        final String description = resultSet.getString("description");
        final LocalDate releaseDate = resultSet.getDate("release_date").toLocalDate();
        long duration = resultSet.getLong("duration");
        int mpaId = resultSet.getInt("rating_mpa");
        return new Film(id, name, description, releaseDate, duration, getLikesByFilmId(id), getMpaById(mpaId), getGenresByFilmId(id));
    }

    private List<Integer> getLikesByFilmId(int filmId) {
        return jdbcTemplate.queryForList("SELECT user_id FROM films_like WHERE film_id = ?", Integer.class, filmId);
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.id, g.name FROM genre as g LEFT JOIN films_genre as fg on g.id = fg.GENRE_ID WHERE film_id = ?",
                this::makeGenre,
                filmId);
    }

    private Mpa getMpaById(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM mpa WHERE id = ?",
                this::makeMpa,
                id);
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
}
