package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> getUsers();

    User getUserById(int userId);

    User createUser(User user);

    User updateUser(User user);

    User deleteUserById(int userId);

    void addFriendship(int userId, int friendId);

    void removeFriendship(int userId, int friendId);

    boolean checkUserExist(int userId);
}
