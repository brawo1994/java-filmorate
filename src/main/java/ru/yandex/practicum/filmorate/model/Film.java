package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private List<Integer> usersLikes;
    private Mpa mpa;
    private List<Genre> genres;
}
