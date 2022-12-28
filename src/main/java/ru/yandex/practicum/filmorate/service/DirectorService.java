package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(int id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotExistException("Director with id: " + id + " does not exist"));
    }

    public Director createDirector(Director director) {
        return getDirectorById(directorStorage.create(director));
    }

    public Director updateDirector(Director director) {
        throwIfDirectorNotExist(director.getId());
        directorStorage.update(director);
        return getDirectorById(director.getId());
    }

    public void deleteDirectorById(int id) {
        throwIfDirectorNotExist(id);
        directorStorage.deleteById(id);
    }

    public void throwIfDirectorNotExist(int id) {
        if (!directorStorage.checkDirectorExist(id))
            throw new NotExistException("Director with id: " + id + " does not exist");
    }
}
