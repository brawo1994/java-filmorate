package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> getDirectors() {
        return directorStorage.getAll();
    }

    public Director getDirectorById(int id) {
        checkDirectorExist(id);
        return directorStorage.getById(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        checkDirectorExist(director.getId());
        return directorStorage.update(director);
    }

    public Director deleteDirectorById(int id) {
        checkDirectorExist(id);
        return directorStorage.deleteById(id);
    }

    public void checkDirectorExist(int id) {
        if (!directorStorage.checkDirectorExist(id))
            throw new NotExistException("Director with id: " + id + " does not exist");
    }
}
