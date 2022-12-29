package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> findAll();

    Optional<Genre> findById(int id);

    boolean checkGenreExist(int id);

    void loadFilmsGenres(List<Film> films);
}
