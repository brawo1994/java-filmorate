package ru.yandex.practicum.filmorate.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.enums.FilmsByDirectorOrderBy;

@Component
public class FilmsByDirectorOrderByEnumConverter implements Converter<String, FilmsByDirectorOrderBy> {

    @Override
    public FilmsByDirectorOrderBy convert(String value) {
        try {
            return FilmsByDirectorOrderBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Incorrect value of parameter sortBy");
        }
    }
}
