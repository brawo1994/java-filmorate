package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
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

    public Film addLike(int filmId, int userId){
        checkFilmExist(filmId);
        filmStorage.getFilmById(filmId).getUsersLikes().add(userId);
        log.info("Пользователь с id: {} поставил лайк фильму с id: {}", userId, filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Film deleteLike(int filmId, int userId){
        checkFilmExist(filmId);
        if (!filmStorage.getFilmById(filmId).getUsersLikes().contains(userId))
            throw new NotExistException("Like from user with id: " + userId + " not found in film with id: " + filmId);
        filmStorage.getFilmById(filmId).getUsersLikes().remove(userId);
        log.info("Пользователь с id: {} удалил лайк у фильма с id: {}", userId, filmId);
        return filmStorage.getFilmById(filmId);
    }

    public List<Film> getPopularFilms(int count){
        return filmStorage.getFilms().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getUsersLikes().size(), o1.getUsersLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmExist(int filmId){
        if (!filmStorage.getFilmsMap().containsKey(filmId))
            throw new NotExistException("Film with id: " + filmId + " does not exist");
    }
}
