package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enums.FilmsByDirectorOrderBy;
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
    private final UserService userService;
    private final DirectorService directorService;
    private final GenreService genreService;
    private final MpaService mpaService;

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        checkFilmExist(filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Film createFilm(Film film) {
        FilmValidate.validate(film);
        if (film.getMpa() != null)
            // Проверяем, что Рейтинг с указанным id присутствует в БД
            mpaService.checkMpaExist(film.getMpa().getId());
        if (film.getGenres() != null) {
            // Проверяем, что Жанры с указанными id присутствует в БД
            for (Genre genre : film.getGenres()) {
                genreService.getById(genre.getId());
            }
        }
        if (film.getDirectors() != null) {
            // Проверяем, что Режиссеры с указанными id присутствует в БД
            for (Director director : film.getDirectors()) {
                directorService.checkDirectorExist(director.getId());
            }
        }
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        FilmValidate.validate(film);
        //Проверяем что редактируемый фильм существует в БД
        checkFilmExist(film.getId());
        return filmStorage.updateFilm(film);
    }

    public Film deleteFilmById(int filmId) {
        checkFilmExist(filmId);
        return filmStorage.deleteFilmById(filmId);
    }

    public Film addLike(int filmId, int userId) {
        checkFilmExist(filmId);
        userService.checkUserExist(List.of(userId));
        if (filmStorage.getFilmById(filmId).getUsersLikes().contains(userId))
            throw new ValidationException("Like from user with id: " + userId + " already exist in film with id: " + filmId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        checkFilmExist(filmId);
        userService.checkUserExist(List.of(userId));
        if (!filmStorage.getFilmById(filmId).getUsersLikes().contains(userId))
            throw new NotExistException("Like from user with id: " + userId + " not found in film with id: " + filmId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getUsersLikes().size(), o1.getUsersLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByDirector(int directorId, FilmsByDirectorOrderBy sortBy) {
        // Проверяем что Режисер с указанным id существует
        directorService.checkDirectorExist(directorId);
        if (sortBy.equals(FilmsByDirectorOrderBy.LIKES)) {
            // Возвращаем отсортированные по лайкам
            return filmStorage.getFilmsByDirectorIdSortedByLike(directorId);
        } else {
            return filmStorage.getFilmsByDirectorIdSortedByReleaseDate(directorId);
        }
    }

    private void checkFilmExist(int filmId) {
        if (!filmStorage.checkFilmExist(filmId))
            throw new NotExistException("Film with id: " + filmId + " does not exist");
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId,friendId);
    }
}
