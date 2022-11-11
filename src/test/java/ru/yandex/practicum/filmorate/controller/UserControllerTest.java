package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private User user;
    private UserController userController;
    private Validator validator;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("test");
        user.setName("Petya");
        user.setBirthday(LocalDate.of(2022, 1, 1));
        userController = new UserController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createAndGetValidUser() {
        userController.createUser(user);
        User returnedUser = userController.getAllUsers().iterator().next();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
        assertEquals(1, userController.getAllUsers().size());
        assertEquals(user.getEmail(), returnedUser.getEmail());
        assertEquals(user.getLogin(), returnedUser.getLogin());
        assertEquals(user.getName(), returnedUser.getName());
        assertEquals(user.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void createUserWithNullEmail() {
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createUserWithNotValidEmail() {
        user.setEmail("email#ya.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createUserWithBlankEmail() {
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createUserWithNullLogin() {
        user.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createUserWithNotValidLogin() {
        user.setLogin("test login");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserWithNullName() {
        user.setName(null);
        userController.createUser(user);
        User returnedUser = userController.getAllUsers().iterator().next();

        assertEquals(1, userController.getAllUsers().size());
        assertEquals(user.getEmail(), returnedUser.getEmail());
        assertEquals(user.getLogin(), returnedUser.getLogin());
        assertEquals(user.getLogin(), returnedUser.getName());
        assertEquals(user.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void createUserWithBlankName() {
        user.setName("");
        userController.createUser(user);
        User returnedUser = userController.getAllUsers().iterator().next();

        assertEquals(1, userController.getAllUsers().size());
        assertEquals(user.getEmail(), returnedUser.getEmail());
        assertEquals(user.getLogin(), returnedUser.getLogin());
        assertEquals(user.getLogin(), returnedUser.getName());
        assertEquals(user.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void createUserWithFutureDate() {
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void updateUser() {
        userController.createUser(user);
        user.setEmail("test_new@ya.ru");
        userController.updateUser(user);
        User returnedUser = userController.getAllUsers().iterator().next();

        assertEquals(userController.getAllUsers().size(), 1);
        assertEquals(user.getEmail(), returnedUser.getEmail());
        assertEquals(user.getLogin(), returnedUser.getLogin());
        assertEquals(user.getName(), returnedUser.getName());
        assertEquals(user.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void updateMissingUser() {
        assertThrows(NotExistException.class, () -> userController.updateUser(user));
    }
}
