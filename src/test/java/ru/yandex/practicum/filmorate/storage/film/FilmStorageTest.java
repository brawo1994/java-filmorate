package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmStorageTest {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private Film film;
    private User user;

    @BeforeEach
    void setup() {
        film = new Film();
        film.setName("Film Name");
        film.setDescription("Film Description");
        film.setDuration(100L);
        film.setReleaseDate(LocalDate.of(2022, 10, 30));
        film.setMpa(new Mpa(1,"G"));
        film.setGenres(null);

        user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("test");
        user.setName("Petya");
        user.setBirthday(LocalDate.of(2022, 1, 1));
        user.setFriends(null);
    }

    @Test
    void addFilmTest() {
        filmStorage.createFilm(film);

        assertEquals(1, film.getId());
    }

    @Test
    void updateFilmTest() {
        filmStorage.createFilm(film);
        film.setName("Film Name_1");
        film.setDescription("Film Description_1");
        filmStorage.updateFilm(film);
        Film newFilm = filmStorage.getFilmById(film.getId());

        assertEquals("Film Name_1", newFilm.getName());
        assertEquals("Film Description_1", newFilm.getDescription());
    }

    @Test
    void deleteFilmTest() {
        filmStorage.createFilm(film);
        filmStorage.deleteFilmById(film.getId());

        assertThrows(NotExistException.class, () -> filmStorage.getFilmById(film.getId()));
    }

    @Test
    void addAndDeleteLike() {
        filmStorage.createFilm(film);
        userStorage.createUser(user);
        filmStorage.addLike(film.getId(), user.getId());
        Film newFilm = filmStorage.getFilmById(film.getId());

        assertEquals(user.getId(), newFilm.getUsersLikes().get(0));

        newFilm = filmStorage.removeLike(film.getId(), user.getId());

        assertTrue(newFilm.getUsersLikes().isEmpty());

    }
}
