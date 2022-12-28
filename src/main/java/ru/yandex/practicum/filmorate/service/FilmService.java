package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.EventHistory;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.FilmsByDirectorOrderBy;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.eventHistory.EventHistoryStorage;
import ru.yandex.practicum.filmorate.model.enums.FilmsSearchBy;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.util.FilmValidate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final DirectorService directorService;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final EventHistoryStorage eventHistoryStorage;


    public List<Film> getFilms() {
        return filmStorage.findFilms();
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotExistException("Film with id: " + filmId + " does not exist"));
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
        int newId = filmStorage.createFilm(film);
        return filmStorage.getFilmById(newId)
                .orElseThrow(() -> new NotExistException("Film with id: " + newId + " does not exist"));
    }

    public Film updateFilm(Film film) {
        FilmValidate.validate(film);
        filmStorage.updateFilm(film);
        return filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotExistException("Film with id: " + film.getId() + " does not exist"));
    }

    public void deleteFilmById(int filmId) {
        throwIfNotExist(filmId);
        filmStorage.deleteFilmById(filmId);
    }

    public void addLike(int filmId, int userId) {
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(OperationType.ADD)
                .entityId(filmId)
                .build());
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        throwIfNotExist(filmId);
        userService.throwIfNotExist(List.of(userId));
        if (!filmStorage.getLikesByFilmId(filmId).contains(userId))
            throw new NotExistException("Like from user with id: " + userId + " not found in film with id: " + filmId);
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(OperationType.REMOVE)
                .entityId(filmId)
                .build());
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer limit, Integer genreId, Integer year) {
        StringBuilder condition = new StringBuilder();

        if (genreId == null && year == null) {
            return filmStorage.findPopular(limit, String.valueOf(condition));

        } else if (year == null) {
            condition.append("WHERE fg.genre_id = ").append(genreId);
            return filmStorage.findPopular(limit, String.valueOf(condition));

        } else if (genreId == null) {
            condition.append("WHERE YEAR(f.release_date) = ").append(year);
            return filmStorage.findPopular(limit, String.valueOf(condition));

        } else {
            condition.append("WHERE fg.genre_id = ").append(genreId)
                    .append("AND YEAR(f.release_date) = ").append(year);
            return filmStorage.findPopular(limit, String.valueOf(condition));
        }
    }

    public List<Film> getFilmsByDirector(int directorId, FilmsByDirectorOrderBy sortBy) {
        // Проверяем что Режисер с указанным id существует
        directorService.checkDirectorExist(directorId);
        if (sortBy.equals(FilmsByDirectorOrderBy.LIKES)) {
            // Возвращаем отсортированные по лайкам
            return filmStorage.findFilmsByDirectorIdSortedByLike(directorId);
        } else {
            return filmStorage.findFilmsByDirectorIdSortedByReleaseDate(directorId);
        }
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.findCommonFilms(userId, friendId);
    }

    public List<Film> getRecommendations(int id) {
        if (id < 1) {
            throw new ValidationException("Попробуйте еще раз, пользователя не существует");
        }
        return filmStorage.getRecommendations(id);
    }

    public List<Film> searchFilms(String query, String by) {
        String[] byArray = by.split(",");
        if (byArray.length > 2) {
            throw new ValidationException("Incorrect value of parameter by");
        }
        if (byArray.length == 2) {
            if (byArray[0].equals(byArray[1])) {
                throw new ValidationException("Incorrect value of parameter by");
            } else {
                return filmStorage.searchFilmsByTitleAndDirector(query);
            }
        } else {
            switch (FilmsSearchBy.valueOf(byArray[0].toUpperCase())) {
                case TITLE:
                    return filmStorage.searchFilmsByTitle(query);
                case DIRECTOR:
                    return filmStorage.searchFilmsByDirector(query);
                default:
                    throw new ValidationException("Incorrect value of parameter by");
            }
        }
    }

    private void throwIfNotExist(int filmId) {
        if (!filmStorage.checkFilmExist(filmId))
            throw new NotExistException("Film with id: " + filmId + " does not exist");
    }
}