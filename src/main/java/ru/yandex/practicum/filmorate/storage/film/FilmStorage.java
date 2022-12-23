package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    List<Film> getRecommendations(int id);

    Collection<Film> getFilms();

    List<Film> getCommonFilms(int userId, int friendId);

    Film getFilmById(int filmId);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film deleteFilmById(int filmId);

    Film addLike(int filmId, int userId);

    Film removeLike(int filmId, int userId);

    List<Film> getFilmsByDirectorIdSortedByLike(int directorId);

    List<Film> getFilmsByDirectorIdSortedByReleaseDate(int directorId);

    boolean checkFilmExist(int filmId);
}
