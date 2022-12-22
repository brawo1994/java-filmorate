package ru.yandex.practicum.filmorate.storage.director.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Director> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM director",
                this::makeDirector);
    }

    @Override
    public Director getById(int id) {
        if (!checkDirectorExist(id))
            throw new NotExistException("Director with id: " + id + " does not exist");
        return jdbcTemplate.queryForObject(
                "SELECT * FROM director WHERE id = ?",
                this::makeDirector,
                id);
    }

    @Override
    public Director create(Director director) {
        KeyHolder generatedId = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO director (name) VALUES(?)",
                    new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, generatedId);
        int createdDirectorId = Objects.requireNonNull(generatedId.getKey()).intValue();
        log.info("Director with id: {} created", createdDirectorId);
        return getById(createdDirectorId);
    }

    @Override
    public Director update(Director director) {
        jdbcTemplate.update(
                "UPDATE director SET name = ? WHERE id = ?",
                director.getName(),
                director.getId());
        log.info("Director with id: {} edited", director.getId());
        return director;
    }

    @Override
    public Director deleteById(int id) {
        Director director = getById(id);
        jdbcTemplate.update(
                "DELETE FROM director WHERE id = ?",
                id);
        log.info("Director with id: {} deleted", id);
        return director;
    }

    @Override
    public boolean checkDirectorExist(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM director WHERE id = ?",
                id);
        return genreRows.next();
    }

    private Director makeDirector(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        return new Director(id, name);
    }
}
