package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class Director {
    private int id;

    @NotBlank(message = "Имя режиссера должно быть указано")
    @Size(max = 50, message = "Имя режиссера не может быть длиннее 50 символов")
    private String name;
}
