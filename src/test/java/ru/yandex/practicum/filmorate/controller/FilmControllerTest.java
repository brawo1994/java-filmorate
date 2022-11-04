package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private Film film;
    private FilmController filmController;
    private Validator validator;

    @BeforeEach
    void setup() {
        film = new Film();
        film.setId(0);
        film.setDuration(100L);
        film.setName("Film Name");
        film.setDescription("Film Description");
        film.setReleaseDate(LocalDate.of(2022, 10, 30));
        filmController = new FilmController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createAndGetValidFilm() {
        filmController.createFilm(film);
        Film returnedFilm = filmController.getAllFilms().iterator().next();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
        assertEquals(1, filmController.getAllFilms().size());
        assertEquals(film.getName(), returnedFilm.getName());
        assertEquals(film.getDescription(), returnedFilm.getDescription());
        assertEquals(film.getReleaseDate(), returnedFilm.getReleaseDate());
        assertEquals(film.getDuration(), returnedFilm.getDuration());
    }

    @Test
    void createFilmWithNullName() {
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithBlankName() {
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithLongDescription() {
        film.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                "Cras non porta nisl. Nunc porttitor sem at massa eleifend, eu condimentum leo convallis." +
                "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus." +
                "Duis vel vulputate ante. Duis scelerisque a massa eu viverra.");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithNullDescription() {
        film.setDescription(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithBlankDescription() {
        film.setDescription("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithEarlyDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 20));

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void createFilmWithNullDate() {
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithNegativeDuration() {
        film.setDuration(-10L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithNullDuration() {
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void updateFilm() {
        filmController.createFilm(film);
        film.setName("Film Name - edited");
        filmController.updateFilm(film);
        Film returnedFilm = filmController.getAllFilms().iterator().next();

        assertEquals(1, filmController.getAllFilms().size());
        assertEquals(film.getName(), returnedFilm.getName());
        assertEquals(film.getDescription(), returnedFilm.getDescription());
        assertEquals(film.getReleaseDate(), returnedFilm.getReleaseDate());
        assertEquals(film.getDuration(), returnedFilm.getDuration());
    }

    @Test
    void updateMissingFilm() {
        assertThrows(NotExistException.class, () -> filmController.updateFilm(film));
    }
}
