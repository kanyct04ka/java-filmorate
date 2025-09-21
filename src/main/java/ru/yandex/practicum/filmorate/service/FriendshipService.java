package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.api.dto.UserDTO;
import ru.yandex.practicum.filmorate.api.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.FriendshipRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

@Slf4j
@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             EventService eventService
    ) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public void addFriend(int userId, int friendId) {

        Optional<User> user = userRepository.getUserById(userId);
        if (user.isEmpty()) {
            logNotFoundError("Попытка добавить друга для не существующего юзера");
        }

        Optional<User> friend = userRepository.getUserById(friendId);
        if (friend.isEmpty()) {
            logNotFoundError("Попытка добавить в качестве друга не существующего юзера");
        }

        if (user.get().getId() == friend.get().getId()) {
            logValidationError("Попытка добавить в качестве друга себя");
        }

        friendshipRepository.saveFriendship(Friendship.builder()
                .user(user.get())
                .friend(friend.get())
                .isFriend(false)
                .build());

        eventService.createEvent(Event.builder()
                .user(user.get())
                .entityId(friendId)
                .type(EventType.FRIEND)
                .operation(EventOperation.ADD)
                .timestamp(Instant.now())
                .build()
        );
    }

    public List<UserDTO> getUserFriends(int userId) {
        if (userRepository.getUserById(userId).isEmpty()) {
            logNotFoundError("Юзера с таким ид не найдено");
        }

        return friendshipRepository.getFriendshipsByUserId(userId)
                .stream()
                .map(Friendship::getFriend)
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public void removeFriendship(int userId, int friendId) {
        Optional<User> user = userRepository.getUserById(userId);
        if (user.isEmpty()) {
            logNotFoundError("Попытка удалить друга для не существующего юзера");
        }

        Optional<User> friend = userRepository.getUserById(friendId);
        if (friend.isEmpty()) {
            logNotFoundError("Попытка удалить в качестве друга не существующего юзера");
        }

        Optional<Friendship> friendship = friendshipRepository.getFriendship(userId, friendId);
        if (friendship.isEmpty()) {
            return;
        }

        friendshipRepository.deleteFriendship(friendship.get());

        eventService.createEvent(Event.builder()
                .user(user.get())
                .entityId(friendId)
                .type(EventType.FRIEND)
                .operation(EventOperation.REMOVE)
                .timestamp(Instant.now())
                .build()
        );
    }

    public List<UserDTO> getCommonFriends(int userOneId, int userTwoId) {
        Optional<User> userOne = userRepository.getUserById(userOneId);
        if (userOne.isEmpty()) {
            logNotFoundError("Юзера с ид " + userOneId + " не существует");
        }

        Optional<User> userTwo = userRepository.getUserById(userTwoId);
        if (userTwo.isEmpty()) {
            logNotFoundError("Юзера с ид " + userTwoId + " не существует");
        }

        List<User> userTwoFriendsId = friendshipRepository.getFriendshipsByUserId(userTwoId)
                .stream()
                .map(Friendship::getFriend)
                .toList();

        return friendshipRepository.getFriendshipsByUserId(userOneId)
                .stream()
                .map(Friendship::getFriend)
                .filter(userTwoFriendsId::contains)
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    private void logValidationError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }

    private void logNotFoundError(String message) {
        log.error(message);
        throw new NotFoundIssueException(message);
    }
}
