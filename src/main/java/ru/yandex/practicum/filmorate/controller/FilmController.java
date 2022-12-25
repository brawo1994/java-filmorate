package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.FilmsByDirectorOrderBy;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public Film deleteFilmById(@PathVariable int id) {
        return filmService.deleteFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(
            @PathVariable int id,
            @PathVariable int userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(
            @PathVariable int id,
            @PathVariable int userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(name = "count", defaultValue = "10", required = false) Integer limit,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(limit, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(defaultValue = "year", required = false) FilmsByDirectorOrderBy sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}