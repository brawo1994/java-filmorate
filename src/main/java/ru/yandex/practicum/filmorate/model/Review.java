package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Review {
    private int reviewId;
    @NotNull(message = "Идентификатор фильма не может отсутствовать")
    private Integer filmId;
    @NotNull(message = "Идентификатор пользователя не может отсутствовать")
    private Integer userId;
    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;
    @NotNull(message = "Тип отзыва не может отсутствовать")
    private Boolean isPositive;
    private int useful;
}
