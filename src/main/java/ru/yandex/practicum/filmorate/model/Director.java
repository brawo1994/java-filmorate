package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class Director {
    int id;
    @NotBlank(message = "Имя режиссера должно быть указано")
    @Size(max = 255, message = "Имя режиссера не может быть длиннее 255 символов")
    String name;
}
