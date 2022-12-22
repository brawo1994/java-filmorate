package ru.yandex.practicum.filmorate.storage.user.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getUsers() {
        return jdbcTemplate.query(
                "SELECT * FROM users",
                this::makeUser);
    }

    @Override
    public User getUserById(int userId) {
        if (!checkUserExist(userId))
            throw new NotExistException("User with id: " + userId + " does not exist");
        return jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                this::makeUser,
                userId);
    }

    @Override
    public User createUser(User user) {
        KeyHolder generatedId = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?,?,?,?)",
                    new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, generatedId);
        user.setId(Objects.requireNonNull(generatedId.getKey()).intValue());
        log.info("User with id: {} created", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ? ",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("User with id: {} edited", user.getId());
        return user;
    }

    @Override
    public User deleteUserById(int userId) {
        User user = getUserById(userId);
        jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                userId);
        log.info("User with id: {} deleted", userId);
        return user;
    }

    @Override
    public void addFriendship(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
                friendId,
                userId);
        String sqlRequestAddFriend = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        if (userRows.first()) {
            // Запрос в друзья уже существует, поэтому добавляем подтвержденную дружбу, и обновляем статус у существующего запроса
            jdbcTemplate.update(
                    sqlRequestAddFriend,
                    userId,
                    friendId,
                    FriendshipStatus.CONFIRMED.toString());
            jdbcTemplate.update(
                    "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.CONFIRMED.toString(),
                    friendId,
                    userId);
        } else {
            // Запрос в друзья не существует, поэтому добавляем не подтвержденную дружбу (запрос на дружбу)
            jdbcTemplate.update(
                    sqlRequestAddFriend,
                    userId,
                    friendId,
                    FriendshipStatus.REQUEST.toString());
        }
    }

    @Override
    public void removeFriendship(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM friends WHERE user_id = ? AND friend_id = ? AND status = ?",
                friendId,
                userId,
                FriendshipStatus.CONFIRMED);
        String sqlRequestDeleteFriend = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        if (userRows.first()) {
            // Дружба подтвержденная, значит нужно не только удалить одностороннюю дружбу, но и изменить статус
            jdbcTemplate.update(
                    sqlRequestDeleteFriend,
                    userId,
                    friendId);
            jdbcTemplate.update(
                    "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.REQUEST.toString(),
                    userId,
                    friendId);
        } else {
            // Дружба не подтвержденная, значит нужно только удалить запрос
            jdbcTemplate.update(
                    sqlRequestDeleteFriend,
                    userId,
                    friendId);
        }
    }

    @Override
    public boolean checkUserExist(int userId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM users WHERE id = ?",
                userId);
        return userRows.next();
    }

    private List<Integer> getFriendsIdByUserId(int userId) {
        return jdbcTemplate.queryForList(
                "SELECT friend_id FROM friends WHERE user_id  = ?",
                Integer.class,
                userId);
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String email = resultSet.getString("email");
        String login = resultSet.getString("login");
        String name = resultSet.getString("name");
        LocalDate birthday = resultSet.getDate("birthday").toLocalDate();
        Set<Integer> friends = new HashSet<>(getFriendsIdByUserId(id));
        return new User(id, email, login, name, birthday, friends);
    }
}
