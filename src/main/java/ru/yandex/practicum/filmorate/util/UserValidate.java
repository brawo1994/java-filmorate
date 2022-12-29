package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;

@Slf4j
public class UserValidate {
    private UserValidate() { throw new IllegalStateException("Utility class"); }
    public static void validate(@Valid @RequestBody User user) {
        //Проверяем только логин на наличие пробелов и Имя на null/Empty, все остальное покрыто аннотациями модели User
        if (user.getLogin().contains(" ")) {
            log.warn("User login has space character, login: {}", user.getLogin());
            throw new ValidationException("Логин содержит пробел");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.warn("User name is null or empty. Use login for name, login: {}", user.getLogin());
        }
    }
}