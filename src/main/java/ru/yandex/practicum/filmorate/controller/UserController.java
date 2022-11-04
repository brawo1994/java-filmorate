package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.UserValidate;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int lastUserId;

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        UserValidate.validate(user);
        int userId = ++lastUserId;
        user.setId(userId);
        users.put(userId,user);
        log.info("User with login: {} added", user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        UserValidate.validate(user);
        int userId = user.getId();
        if (users.containsKey(userId)) {
            users.put(userId, user);
            log.info("User with login: {} edited", user.getLogin());
            return user;
        } else
            throw new NotExistException("User with login: " + user.getLogin() + " does not exist");
    }
}
