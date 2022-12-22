package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    public Mpa getById(int id) {
        checkMpaExist(id);
        return mpaStorage.getById(id);
    }

    public void checkMpaExist(int id) {
        if (!mpaStorage.checkMpaExist(id))
            throw new NotExistException("MPA with id: " + id + " does not exist");
    }
}
