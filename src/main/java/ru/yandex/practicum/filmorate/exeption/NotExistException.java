package ru.yandex.practicum.filmorate.exeption;

public class NotExistException extends RuntimeException {
    public NotExistException(String message) {
        super(message);
    }
}
