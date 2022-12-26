package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.EventHistory;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event_history.EventHistoryStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.UserValidate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final EventHistoryStorage eventHistoryStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Integer userId) {
        checkUserExist(List.of(userId));
        return userStorage.getUserById(userId);
    }

    public User createUser(User user) {
        UserValidate.validate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        UserValidate.validate(user);
        checkUserExist(List.of(user.getId()));
        return userStorage.updateUser(user);
    }

    public User deleteUserById(int userId) {
        checkUserExist(List.of(userId));
        return userStorage.deleteUserById(userId);
    }

    public User addFriend(int userId, int friendId) {
        checkUsersDifferent(userId, friendId);
        checkUserExist(List.of(userId, friendId));
        if (userStorage.getUserById(userId).getFriends().contains(friendId))
            throw new ValidationException("Users with id " + userId + " and " + friendId + " already friends");
        userStorage.addFriendship(userId, friendId);
        log.info("Users with id: {} and {} have become friends", userId, friendId);
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(userId)
                .eventType(EventType.FRIEND)
                .operation(OperationType.ADD)
                .entityId(friendId)
                .build());
        return userStorage.getUserById(userId);
    }

    public User deleteFriend(int userId, int friendId) {
        checkUsersDifferent(userId, friendId);
        checkUserExist(List.of(userId, friendId));
        if (!userStorage.getUserById(userId).getFriends().contains(friendId))
            throw new ValidationException("Users with id " + userId + " and " + friendId + " are not friends");
        userStorage.removeFriendship(userId, friendId);
        log.info("Users with id: {} and {} not friends anymore", userId, friendId);
        eventHistoryStorage.save(EventHistory.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(userId)
                .eventType(EventType.FRIEND)
                .operation(OperationType.REMOVE)
                .entityId(friendId)
                .build());
        return userStorage.getUserById(userId);
    }

    public List<User> getUserFriends(int userId) {
        checkUserExist(List.of(userId));
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int firstUserId, int secondUserId) {
        checkUserExist(List.of(firstUserId, secondUserId));
        checkUsersDifferent(firstUserId, secondUserId);
        return userStorage.getUserById(firstUserId).getFriends().stream()
                .filter(friendId -> userStorage.getUserById(secondUserId).getFriends().contains(friendId))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public void checkUserExist(List<Integer> userIdList) {
        for (Integer userId : userIdList) {
            if (!userStorage.checkUserExist(userId))
                throw new NotExistException("User with id: " + userId + " does not exist");
        }
    }

    public List<EventHistory> getEHistoryByUserId(int id) {
        getUserById(id);
        return eventHistoryStorage.findByUserId(id);
    }

    private void checkUsersDifferent(int firstUserId, int secondUserId) {
        if (firstUserId == secondUserId)
            throw new ValidationException("User cannot be friends with themselves");
    }
}
