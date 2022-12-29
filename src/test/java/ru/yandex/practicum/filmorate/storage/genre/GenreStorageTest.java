package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreStorageTest {

    @Autowired
    private final GenreStorage genreStorage;

    @Test
    void getAllGenres() {
        List<Genre> genres = genreStorage.findAll();
        Assertions.assertThat(genres)
                .isNotEmpty()
                .extracting(Genre::getName)
                .containsAll(Arrays.asList("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик"));
    }

    @Test
    void getGenreById() {
        Genre newGenre = genreStorage.findById(1)
                .orElseThrow(() -> new NotExistException("Mpa does not exist"));
        assertEquals(1, newGenre.getId());
        assertEquals("Комедия", newGenre.getName());
    }
}
