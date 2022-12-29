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
import ru.yandex.practicum.filmorate.model.enums.FilmsSearchBy;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.eventHistory.EventHistoryStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.util.FilmValidate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;//получаем список фильмов с жанрами
    private final DirectorStorage directorStorage;//получаем список фильмов с режиссерами
    private final UserService userService;
    private final DirectorService directorService;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final EventHistoryStorage eventHistoryStorage;


    public List<Film> getFilms() {
        List<Film> films = filmStorage.findFilms();
        loadInformationFilms(films);
        return films;
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotExistException("Film with id: " + filmId + " does not exist"));
    }

    public Film createFilm(Film film) {
        FilmValidate.validate(film);
        if (film.getMpa() != null)
            // Проверяем, что Рейтинг с указанным id присутствует в БД
            mpaService.throwIfMpaNotExist(film.getMpa().getId());
        if (film.getGenres() != null) {
            // Проверяем, что Жанры с указанными id присутствует в БД
            for (Genre genre : film.getGenres()) {
                genreService.throwIfGenreNotExist(genre.getId());
            }
        }
        if (film.getDirectors() != null) {
            // Проверяем, что Режиссеры с указанными id присутствует в БД
            for (Director director : film.getDirectors()) {
                directorService.throwIfDirectorNotExist(director.getId());
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
        userService.throwIfUserNotExist(List.of(userId));
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
        List<Film> films;

        if (genreId == null && year == null) {
            films = filmStorage.findPopular(limit, String.valueOf(condition));
        } else if (year == null) {
            condition.append("WHERE fg.genre_id = ").append(genreId);
            films = filmStorage.findPopular(limit, String.valueOf(condition));
        } else if (genreId == null) {
            condition.append("WHERE YEAR(f.release_date) = ").append(year);
            films = filmStorage.findPopular(limit, String.valueOf(condition));
        } else {
            condition.append("WHERE fg.genre_id = ").append(genreId)
                    .append("AND YEAR(f.release_date) = ").append(year);
            films = filmStorage.findPopular(limit, String.valueOf(condition));
        }
        loadInformationFilms(films);
        return films;
    }

    public List<Film> getFilmsByDirector(int directorId, FilmsByDirectorOrderBy sortBy) {
        // Проверяем что Режиссер с указанным id существует
        directorService.throwIfDirectorNotExist(directorId);
        List<Film> films;
        if (sortBy.equals(FilmsByDirectorOrderBy.LIKES)) {
            // Возвращаем отсортированные по лайкам
            films = filmStorage.findFilmsByDirectorIdSortedByLike(directorId);
        } else {
            films = filmStorage.findFilmsByDirectorIdSortedByReleaseDate(directorId);
        }
        loadInformationFilms(films);
        return films;
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> films = filmStorage.findCommonFilms(userId, friendId);
        loadInformationFilms(films);
        return films;
    }

    public List<Film> getRecommendations(int id) {
        if (id < 1) {
            throw new ValidationException("Попробуйте еще раз, пользователя не существует");
        }
        List<Film> films = filmStorage.getRecommendations(id);
        loadInformationFilms(films);
        return films;
    }

    public List<Film> searchFilms(String query, String by) {
        String[] byArray = by.split(",");
        List<Film> films;
        if (byArray.length > 2) {
            throw new ValidationException("Incorrect value of parameter by");
        }
        if (byArray.length == 2) {
            if (byArray[0].equals(byArray[1])) {
                throw new ValidationException("Incorrect value of parameter by");
            } else {
                films = filmStorage.searchFilmsByTitleAndDirector(query);
                loadInformationFilms(films);
                return films;
            }
        } else {
            switch (FilmsSearchBy.valueOf(byArray[0].toUpperCase())) {
                case TITLE:
                    films = filmStorage.searchFilmsByTitle(query);
                    loadInformationFilms(films);
                    return films;
                case DIRECTOR:
                    films = filmStorage.searchFilmsByDirector(query);
                    loadInformationFilms(films);
                    return films;
                default:
                    throw new ValidationException("Incorrect value of parameter by");
            }
        }
    }

    private void throwIfNotExist(int filmId) {
        if (!filmStorage.checkFilmExist(filmId))
            throw new NotExistException("Film with id: " + filmId + " does not exist");
    }

    private void loadInformationFilms(List<Film> films) {
        if (films.isEmpty()) { //Если фильмов нет, то не обращаемся за получением доп. информации
            log.info("filmStorage getRecommendation not exist");
            return;
        }
        log.info("filmStorage find all Films. films.size()  {}", films.size());
        genreStorage.loadFilmsGenres(films);//получаем жанры для всего списка фильмов
        directorStorage.loadFilmsDirectors(films);//получаем режиссеров для всего списка фильмов
        filmStorage.loadFilmsLikes(films);//получаем оценки для всего списка фильмов
    }
}