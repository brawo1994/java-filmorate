package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;

public interface FilmStorage {

    Collection<Film> getFilms();

    Map<Integer, Film> getFilmsMap();

    Film getFilmById(int filmId);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film deleteFilmById(int filmId);
}
