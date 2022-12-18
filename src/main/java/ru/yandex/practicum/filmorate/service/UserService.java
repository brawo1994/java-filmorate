package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.UserValidate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Integer userId) {
        return userStorage.getUserById(userId);
    }

    public User createUser(User user) {
        UserValidate.validate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        UserValidate.validate(user);
        return userStorage.updateUser(user);
    }

    public User deleteUserById(int userId) {
        return userStorage.deleteUserById(userId);
    }

    public User addFriend(int userId, int friendId) {
        if (userId == friendId)
            throw new ValidationException("User cannot be friends with himself");
        checkUserExist(List.of(userId,friendId));
        if (userStorage.getUserById(userId).getFriends().contains(friendId) && userStorage.getUserById(friendId).getFriends().contains(userId))
            throw new ValidationException("Users with id {} and {} already friends");
        userStorage.addFriendship(userId, friendId);
        log.info("Пользователи с id: {} и {} стали друзьями", userId, friendId);
        return userStorage.getUserById(userId);
    }

    public User deleteFriend(int userId, int friendId) {
        if (userId == friendId)
            throw new ValidationException("User cannot be friends with himself");
        checkUserExist(List.of(userId,friendId));
        if (!userStorage.getUserById(userId).getFriends().contains(friendId) && !userStorage.getUserById(friendId).getFriends().contains(userId))
            throw new ValidationException("Users with id {} and {} are not friends");
        userStorage.removeFriendship(userId, friendId);
        log.info("Пользователи с id: {} и {} больше не друзья", userId, friendId);
        return userStorage.getUserById(userId);
    }

    public List<User> getUserFriends(int userId) {
        checkUserExist(List.of(userId));
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int firstUserId, int secondUserId) {
        checkUserExist(List.of(firstUserId,secondUserId));
        if (firstUserId == secondUserId)
            throw new ValidationException("User cannot be friends with himself");
        return userStorage.getUserById(firstUserId).getFriends().stream()
                .filter(friendId -> userStorage.getUserById(secondUserId).getFriends().contains(friendId))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    private void checkUserExist(List<Integer> userIdList){
        for (Integer userId : userIdList){
            userStorage.getUserById(userId); // Если пользователя не существует, вылетит исключение
        }
    }
}
