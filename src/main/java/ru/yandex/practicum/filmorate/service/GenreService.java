package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getAll() {
        return genreStorage.findAll();
    }

    public Genre getById(int id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotExistException("Genre with id: " + id + " does not exist"));
    }

    public void throwIfGenreNotExist(int id) {
        if (!genreStorage.checkGenreExist(id))
            throw new NotExistException("Genre with id: " + id + " does not exist");
    }
}
