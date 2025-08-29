package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.service.UserService;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {
    private final UserService userService;

    @Autowired
    public FriendshipRowMapper(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Friendship mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Friendship.builder()
                .id(resultSet.getInt("id"))
                .user(userService.getUserById(resultSet.getInt("user_id")))
                .friend(userService.getUserById(resultSet.getInt("friend_id")))
                .isFriend(resultSet.getBoolean("is_friend"))
                .build();
    }
}
