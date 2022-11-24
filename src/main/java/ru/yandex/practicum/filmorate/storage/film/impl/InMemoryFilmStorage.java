package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int lastFilmId;

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Map<Integer, Film> getFilmsMap() {
        return films;
    }

    @Override
    public Film getFilmById(int filmId) {
        if (films.containsKey(filmId))
            return films.get(filmId);
        else
            throw new NotExistException("Film with id: " + filmId + " does not exist");
    }

    @Override
    public Film createFilm(Film film) {
        int filmId = ++lastFilmId;
        film.setId(filmId);
        films.put(filmId,film);
        log.info("Film with name: {} added to InMemoryStorage", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int filmId = film.getId();
        if (films.containsKey(filmId)) {
            films.put(filmId, film);
            log.info("Film with name: {} edited", film.getName());
            return film;
        } else
            throw new NotExistException("Film with name: " + film.getName() + " does not exist");
    }

    @Override
    public Film deleteFilmById(int filmId) {
        if (films.containsKey(filmId)) {
            Film film = films.get(filmId);
            films.remove(filmId);
            log.info("Film with id: {} deleted", filmId);
            return film;
        } else
            throw new NotExistException("Film with id: " + filmId + " does not exist");
    }
}
