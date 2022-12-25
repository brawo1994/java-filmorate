package ru.yandex.practicum.filmorate.model;

import lombok.*;

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
