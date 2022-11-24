package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    int id;
    @Email(message = "Email не соответствует формату")
    @NotBlank(message = "Email не может быть пустым")
    String email;
    @NotBlank(message = "Логин не может быть пустым")
    String login;
    String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения не может отсутствовать")
    LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();
}
