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
        final List<Film> films =
                jdbcTemplate.query(sql, (rs, rowNum) -> makeObjectFilm(rs), id, id);
        if (films.size() < 1) { //Если рекомендованных фильмов нет, то не обращаемся за получением доп. информации
            log.info("filmStorage getRecommendation not exist");
            return films;
        }
        log.info("filmStorage getRecommendation for user id = {}", id);
        loadFilmsGenres(films);//получаем жанры для всего списка фильмов
        loadFilmsDirectors(films);//получаем режиссеров для всего списка фильмов
        loadFilmsLikes(films);//получаем оценки для всего списка фильмов
        return films;
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

    private void loadFilmsLikes(List<Film> films) {
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbcTemplate.query(
                String.format("select FILM_ID, USER_ID from FILMS_LIKE " +
                        "      where FILM_ID in (%s)", inSql),
                ids.toArray(),
                (rs, rowNum) -> makeFilmListWithLikes(rs, films));
    }

    private Film makeFilmListWithLikes(ResultSet rs, List<Film> films) throws SQLException {
        int filmId = rs.getInt("film_id");
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        filmMap.get(filmId).addLikes(rs.getInt("user_id"));
        return filmMap.get(filmId);
    }


    private void loadFilmsDirectors(List<Film> films) {
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbcTemplate.query(
                String.format("select FILM_ID, D.ID, D.NAME from DIRECTOR D " +
                        "      join FILMS_DIRECTOR FD on D.ID = FD.DIRECTOR_ID " +
                        "      where FILM_ID in (%s)", inSql),
                ids.toArray(),
                (rs, rowNum) -> makeFilmListWithDirectors(rs, films));
    }

    private Film makeFilmListWithDirectors(ResultSet rs, List<Film> films) throws SQLException {
        int id = rs.getInt("film_id");
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        filmMap.get(id).addDirector(new Director(rs.getInt("id"), rs.getString("name")));
        return filmMap.get(id);
    }

    private void loadFilmsGenres(List<Film> films) {
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbcTemplate.query(
                String.format("select FILM_ID, G.ID, G.NAME from GENRE G " +
                        "            left join FILMS_GENRE FG on G.ID = FG.GENRE_ID " +
                        "             where FILM_ID in (%s)", inSql),
                ids.toArray(),
                (rs, rowNum) -> makeFilmListWithGenre(rs, films));
    }

    private Film makeFilmListWithGenre(ResultSet rs, List<Film> films) throws SQLException {
        int filmId = rs.getInt("film_id");
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        filmMap.get(filmId).addGenre(new Genre(rs.getInt("id"), rs.getString("name")));
        return filmMap.get(filmId);
    }

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
    public List<Film> getPopular(Integer limit, String condition) {
        String sqlQuery = "SELECT f.id, " +
                "f.name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "f.rating_mpa " +
                "FROM films AS f " +
                "LEFT JOIN films_genre AS fg ON f.id = fg.film_id " +
                "LEFT JOIN films_like AS fl ON f.id = fl.film_id " +
                condition +
                "GROUP BY f.id, f.name,f.description, f.release_date, f.duration, f.rating_mpa " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, this::makeFilm, limit);

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
                "merge INTO films_like (film_id, user_id) VALUES (?, ?)",
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

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        String likeValue = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(
                "SELECT f.*, COUNT(l.film_id) FROM films as f " +
                        "LEFT JOIN films_like as l on l.film_id = f.id " +
                        "WHERE LOWER(f.name) like ? " +
                        "GROUP BY f.id ORDER BY COUNT(l.film_id) DESC",
                this::makeFilm,
                likeValue);
    }

    @Override
    public List<Film> searchFilmsByDirector(String query) {
        String likeValue = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(
                "SELECT DISTINCT f.*, COUNT(l.film_id) FROM films as f " +
                        "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                        "LEFT JOIN director as d ON d.id = fd.director_id " +
                        "LEFT JOIN films_like as l on l.film_id = f.id " +
                        "WHERE LOWER(d.name) like ? " +
                        "GROUP BY f.id ORDER BY COUNT(l.film_id) DESC",
                this::makeFilm,
                likeValue);
    }

    @Override
    public List<Film> searchFilmsByTitleAndDirector(String query) {
        String likeValue = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(
                "SELECT DISTINCT f.*, COUNT(l.film_id) FROM films as f " +
                        "LEFT JOIN films_director as fd ON fd.film_id = f.id " +
                        "LEFT JOIN director as d ON d.id = fd.director_id " +
                        "LEFT JOIN films_like as l on l.film_id = f.id " +
                        "WHERE LOWER(d.name) like ? OR LOWER(f.name) like ?" +
                        "GROUP BY f.id ORDER BY COUNT(l.film_id) DESC",
                this::makeFilm,
                likeValue,
                likeValue);
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