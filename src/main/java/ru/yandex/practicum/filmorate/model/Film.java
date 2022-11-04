package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Film {
    int id;
    @NotBlank(message = "Наименование фильма должно быть указано")
    String name;
    @NotBlank
    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    String description;
    @NotNull(message = "Дата релиза должна быть указана")
    LocalDate releaseDate;
    @NotNull(message = "Продолжительность фильма должна быть указана")
    @PositiveOrZero(message = "Продолжительность фильма не может быть отрицательной")
    Long duration;
}
