package ru.yandex.practicum.filmorate.storage.genre.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM genre",
                this::makeGenre);
    }

    @Override
    public Genre getById(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM genre WHERE id = ?",
                this::makeGenre,
                id);
    }

    @Override
    public boolean checkGenreExist(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM genre WHERE id = ?",
                id);
        return genreRows.next();
    }

    @Override
    public void loadFilmsGenres(List<Film> films) {
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

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        return new Genre(id, name);
    }
}
