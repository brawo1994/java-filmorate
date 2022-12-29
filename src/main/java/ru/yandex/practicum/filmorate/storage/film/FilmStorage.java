package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    List<Film> getRecommendations(int id);

    List<Film> findFilms();

    List<Film> findCommonFilms(int userId, int friendId);

    Optional<Film> getFilmById(int filmId);

    int createFilm(Film film);

    void updateFilm(Film film);

    void deleteFilmById(int filmId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> findFilmsByDirectorIdSortedByLike(int directorId);

    List<Film> findFilmsByDirectorIdSortedByReleaseDate(int directorId);

    List<Film> findPopular(Integer limit, String condition);

    boolean checkFilmExist(int filmId);

    List<Film> searchFilmsByTitle(String query);

    List<Film> searchFilmsByDirector(String query);

    List<Film> searchFilmsByTitleAndDirector(String query);

    void loadFilmsLikes(List<Film> films);

    List<Integer> getLikesByFilmId(int filmId);
}