package ru.yandex.practicum.filmorate.storage.eventHistory.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventHistory;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.eventHistory.EventHistoryStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EHistoryDbStorage implements EventHistoryStorage {
    private final JdbcTemplate jdbcTemplate;

    public EHistoryDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<EventHistory> findByUserId(int id) {
        String sqlQuery = "SELECT * " +
                "FROM event_history WHERE user_id = ? ORDER BY event_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToEventHistory, id);
    }

    @Override
    public void save(EventHistory eventHistory) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("event_history")
                .usingGeneratedKeyColumns("event_id");
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("timestamp", eventHistory.getTimestamp());
        eventMap.put("user_id", eventHistory.getUserId());
        eventMap.put("entity_id", eventHistory.getEntityId());
        eventMap.put("operation", eventHistory.getOperation().name());
        eventMap.put("event_type", eventHistory.getEventType().name());

        simpleJdbcInsert.executeAndReturnKey(eventMap);
    }

    private EventHistory mapRowToEventHistory(ResultSet resultSet, int rowNum)
            throws SQLException {
        return EventHistory.builder()
                .eventId(resultSet.getInt("event_id"))
                .timestamp(resultSet.getLong("timestamp"))
                .userId(resultSet.getInt("user_id"))
                .entityId(resultSet.getInt("entity_id"))
                .operation(OperationType.valueOf(resultSet.getString("operation")))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .build();
    }
}