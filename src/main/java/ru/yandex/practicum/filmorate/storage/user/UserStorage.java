package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {

    Collection<User> getUsers();

    Map<Integer, User> getUsersMap();

    User getUserById(int userId);

    User createUser(User user);

    User updateUser(User user);
}
