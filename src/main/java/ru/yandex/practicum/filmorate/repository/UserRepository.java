package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;


@Repository
public class UserRepository extends BaseRepository<User> {

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> rowMapper) {
        super(jdbc, rowMapper);
    }

    public List<User> getAllUsers() {
        String query = "select * from users";
        return getRecords(query);
    }

    public Optional<User> getUserById(int id) {
        String query = "select * from users where id = ?";
        return getRecord(query, id);
    }

    public Optional<User> getUserByEmail(String email) {
        String query = "select * from users where email = ?";
        return getRecord(query, email);
    }

    public User saveUser(User user) {
        String query = "insert into users (email, login, name, birthday)"
                       + "values (?, ?, ?, ?)";
        int id = insert(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        ).intValue();

        user.setId(id);
        return user;
    }

    public User updateUser(User user) {
        String query = "update users set email = ?, login = ?, name = ?, birthday = ? where id = ?";
        int result = update(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (result == 0) {
            throw new NotFoundIssueException("Пользователь с id = " + user.getId() + " не найден");
        }

        return getUserById(user.getId()).orElseThrow();
    }

    public void deleteUser(int id) {
        Optional<User> user = getUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundIssueException("Пользователь с id = " + id + " не найден");
        }

        String queryEvents = "delete from events where user_id = ?";
        delete(queryEvents, id);

        String queryEvents2 = "delete from events where type = 'REVIEW' and entity_id in (select id from reviews where user_id = ?)";
        delete(queryEvents2, id);

        String queryFriends1 = "delete from user_friends where user_id = ?";
        delete(queryFriends1, id);

        String queryFriends2 = "delete from user_friends where friend_id = ?";
        delete(queryFriends2, id);

        String queryLikes = "delete from likes where user_id = ?";
        delete(queryLikes, id);

        String queryReviews = "delete from reviews where user_id = ?";
        delete(queryReviews, id);

        String queryReviewLikes = "delete from review_likes where user_id = ?";
        delete(queryReviewLikes, id);

        String queryUserS = "delete from users where id = ?";
        delete(queryUserS, id);
    }
}