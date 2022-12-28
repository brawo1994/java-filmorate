package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    int id;
    @Email(message = "Email не соответствует формату")
    @NotBlank(message = "Email не может быть пустым")
    @Size(max = 50, message = "Email не может быть длиннее 50 символов")
    String email;
    @NotBlank(message = "Логин не может быть пустым")
    @Size(max = 50, message = "Логин не может быть длиннее 50 символов")
    String login;
    @Size(max = 50, message = "Имя не может быть длиннее 50 символов")
    String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения не может отсутствовать")
    LocalDate birthday;
}
