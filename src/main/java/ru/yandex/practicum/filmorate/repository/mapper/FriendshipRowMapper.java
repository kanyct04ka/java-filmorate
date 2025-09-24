package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {

    @Override
    public Friendship mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Friendship.builder()
                .user(User.builder()
                        .id(resultSet.getInt("user_id"))
                        .email(resultSet.getString("user_email"))
                        .login(resultSet.getString("user_login"))
                        .name(resultSet.getString("user_name"))
                        .birthday(resultSet.getDate("user_birthday").toLocalDate())
                        .build())
                .friend(User.builder()
                        .id(resultSet.getInt("friend_id"))
                        .email(resultSet.getString("friend_email"))
                        .login(resultSet.getString("friend_login"))
                        .name(resultSet.getString("friend_name"))
                        .birthday(resultSet.getDate("friend_birthday").toLocalDate())
                        .build())
                .isFriend(resultSet.getBoolean("is_friend"))
                .build();
    }
}
