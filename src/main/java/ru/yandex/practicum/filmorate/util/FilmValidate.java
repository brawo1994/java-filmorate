package ru.yandex.practicum.filmorate.util;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.time.LocalDate;

@Slf4j
public class FilmValidate {
    private static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private FilmValidate() { throw new IllegalStateException("Utility class"); }
    public static void validate(@Valid @RequestBody Film film) {
        //Проверяем только дату релиза, все остальное покрыто аннотациями модели Film
        if (film.getReleaseDate().isBefore(MIN_FILM_RELEASE_DATE)) {
            log.warn("Film release date is before: {}, release is: {}", MIN_FILM_RELEASE_DATE,film.getReleaseDate());
            throw new ValidationException("Дата релиза фильма раньше чем " + MIN_FILM_RELEASE_DATE);
        }
    }
}
