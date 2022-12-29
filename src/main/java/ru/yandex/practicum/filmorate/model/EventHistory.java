package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EventHistory {
    private int eventId;

    private long timestamp;

    private int userId;

    private int entityId;

    private OperationType operation;

    private EventType eventType;
}
