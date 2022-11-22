package ru.yandex.practicum.filmorate.storage.user.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int lastUserId;

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Map<Integer, User> getUsersMap() {
        return users;
    }

    @Override
    public User getUserById(int userId) {
        if (users.containsKey(userId))
            return users.get(userId);
        else
            throw new NotExistException("User with id: " + userId + " does not exist");
    }

    @Override
    public User createUser(User user) {
        int userId = ++lastUserId;
        user.setId(userId);
        users.put(userId,user);
        log.info("User with login: {} added to InMemoryStorage", user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {
        int userId = user.getId();
        if (users.containsKey(userId)) {
            users.put(userId, user);
            log.info("User with login: {} edited", user.getLogin());
            return user;
        } else
            throw new NotExistException("User with login: " + user.getLogin() + " does not exist");
    }
}
