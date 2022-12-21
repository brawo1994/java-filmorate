package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.util.FilmValidate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film createFilm(Film film) {
        FilmValidate.validate(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        FilmValidate.validate(film);
        return filmStorage.updateFilm(film);
    }

    public Film deleteFilmById(int filmId) {
        checkFilmExist(filmId);
        return filmStorage.deleteFilmById(filmId);
    }

    public Film addLike(int filmId, int userId){
        checkFilmExist(filmId);
        if (filmStorage.getFilmById(filmId).getUsersLikes().contains(userId))
            throw new ValidationException("Like from user with id: " + userId + " already exist in film with id: " + filmId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId){
        checkFilmExist(filmId);
        if (!filmStorage.getFilmById(filmId).getUsersLikes().contains(userId))
            throw new NotExistException("Like from user with id: " + userId + " not found in film with id: " + filmId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count){
        return filmStorage.getFilms().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getUsersLikes().size(), o1.getUsersLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmExist(int filmId){
        filmStorage.getFilmById(filmId); // Если фильм отсутствует в БД, вылетит исключение
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId,friendId);
    }
}
