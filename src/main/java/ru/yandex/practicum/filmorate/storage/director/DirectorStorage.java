package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

    List<Director> findAll();

    Optional<Director> findById(int id);

    int create(Director director);

    void update(Director director);

    void deleteById(int id);

    boolean checkDirectorExist(int id);

    void loadFilmsDirectors(List<Film> films);
}
