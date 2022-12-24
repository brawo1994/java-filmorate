package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private List<Integer> usersLikes = new ArrayList<>();
    private Mpa mpa;
    private List<Genre> genres= new LinkedList<>();//прилетал NULL в проверку жанров
    private List<Director> directors = new ArrayList<>();

    public Film(int id, String name, String description, LocalDate releaseDate, long duration, Mpa mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void addDirector(Director director) {
    directors.add(director);
    }

    public void addLikes(Integer userId) {
        usersLikes.add(userId);
    }
}
