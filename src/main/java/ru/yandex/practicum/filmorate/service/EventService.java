package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.api.dto.EventDTO;
import ru.yandex.practicum.filmorate.api.mapper.EventMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.repository.EventRepository;

@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventDTO createEvent(Event event) {
        event = eventRepository.saveEvent(event);
        log.info("Зафиксировано событие: {}", event.toString());
        return EventMapper.mapToEventDTO(event);
    }

    public List<EventDTO> getUserFeed(int userId) {
        return eventRepository.getEventsByUserId(userId)
                .stream()
                .map(EventMapper::mapToEventDTO)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            if (list.isEmpty()) {
                                throw new NotFoundIssueException("Событий для пользователя не найдено");
                            }
                            return list;
                        }
                ));
    }
}
