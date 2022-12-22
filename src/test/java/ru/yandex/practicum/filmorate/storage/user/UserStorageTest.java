package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserStorageTest {
    private final UserStorage userStorage;
    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        user1 = new User();
        user1.setEmail("test1@ya.ru");
        user1.setLogin("test1");
        user1.setName("Petya1");
        user1.setBirthday(LocalDate.of(2022, 1, 1));
        user1.setFriends(null);

        user2 = new User();
        user2.setEmail("test2@ya.ru");
        user2.setLogin("test2");
        user2.setName("Petya2");
        user2.setBirthday(LocalDate.of(2022, 1, 1));
        user2.setFriends(null);
    }

    @AfterEach
    void clear() {
        for (User user : userStorage.getUsers()) {
            userStorage.deleteUserById(user.getId());
        }
    }

    @Test
    void getUsersTest() {
        userStorage.createUser(user1);
        userStorage.createUser(user2);

        assertEquals(2, userStorage.getUsers().size());
    }

    @Test
    void getUserByIdTest() {
        userStorage.createUser(user1);

        assertEquals(user1.getId(), userStorage.getUserById(user1.getId()).getId());
        assertEquals(user1.getEmail(), userStorage.getUserById(user1.getId()).getEmail());
        assertEquals(user1.getLogin(), userStorage.getUserById(user1.getId()).getLogin());
        assertEquals(user1.getName(), userStorage.getUserById(user1.getId()).getName());
        assertEquals(user1.getBirthday(), userStorage.getUserById(user1.getId()).getBirthday());
        assertTrue(userStorage.getUserById(user1.getId()).getFriends().isEmpty());
    }

    @Test
    void updateUserTest() {
        userStorage.createUser(user1);
        user1.setName("Исправленное Имя");
        user1.setEmail("change_email@mail.ru");
        userStorage.updateUser(user1);

        assertEquals("Исправленное Имя", userStorage.getUserById(user1.getId()).getName());
        assertEquals("change_email@mail.ru", userStorage.getUserById(user1.getId()).getEmail());
    }

    @Test
    void deleteUserByIdTest() {
        userStorage.createUser(user1);
        userStorage.deleteUserById(user1.getId());

        assertThrows(NotExistException.class, () -> userStorage.getUserById(user1.getId()));
    }

    @Test
    void addFriendshipTest() {
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.addFriendship(user1.getId(), user2.getId());

        assertTrue(userStorage.getUserById(user1.getId()).getFriends().contains(user2.getId()));
    }

    @Test
    void removeFriendshipTest() {
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.addFriendship(user1.getId(), user2.getId());
        userStorage.removeFriendship(user1.getId(), user2.getId());

        assertFalse(userStorage.getUserById(user1.getId()).getFriends().contains(user2.getId()));
    }
}