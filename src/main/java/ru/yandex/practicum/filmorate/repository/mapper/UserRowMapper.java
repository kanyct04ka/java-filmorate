package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;


@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        User user = User.builder()
                .id(resultSet.getInt("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
//                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
        if (resultSet.getDate("birthday") != null) {
            user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        }

        return user;
    }
}
