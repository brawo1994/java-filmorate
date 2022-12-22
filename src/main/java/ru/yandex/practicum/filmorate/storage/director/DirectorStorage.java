package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {

    Collection<Director> getAll();

    Director getById(int id);

    Director create(Director director);

    Director update(Director director);

    Director deleteById(int id);

    boolean checkDirectorExist(int id);
}
