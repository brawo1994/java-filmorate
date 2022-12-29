package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    public Mpa getById(int id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotExistException("MPA with id: " + id + " does not exist"));
    }

    public void throwIfMpaNotExist(int id) {
        if (!mpaStorage.checkMpaExist(id))
            throw new NotExistException("MPA with id: " + id + " does not exist");
    }
}
