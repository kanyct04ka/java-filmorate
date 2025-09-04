package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.Optional;

@Repository
public class FriendshipRepository extends BaseRepository<Friendship> {

    public FriendshipRepository(JdbcTemplate jdbc, RowMapper<Friendship> rowMapper) {
        super(jdbc, rowMapper);
    }

    public Friendship saveFriendship(Friendship friendship) {
        String query = "insert into user_friends (user_id, friend_id, is_friend)"
                + "values (?, ?, ?)";

        int id = insert(query,
                friendship.getUser().getId(),
                friendship.getFriend().getId(),
                friendship.getIsFriend()
        ).intValue();

        friendship.setId(id);
        return friendship;
    }

    public List<Friendship> getFriendshipsByUserId(int id) {
        String query = "select uf.id, uf.user_id, uf.friend_id, uf.is_friend,"
                + " u.login as user_login, u.email as user_email, u.name as user_name, u.birthday as user_birthday,"
                + " f.login as friend_login, f.email as friend_email, f.name as friend_name, f.birthday as friend_birthday"
                + " from user_friends uf"
                + " inner join users u on uf.user_id = u.id"
                + " inner join users f on uf.friend_id = f.id"
                + " where u.id = ?";
        return getRecords(query, id);
    }

    public Optional<Friendship> getFriendship(int userId, int friendId) {
        String query = "select uf.id, uf.user_id, uf.friend_id, uf.is_friend,"
                + " u.login as user_login, u.email as user_email, u.name as user_name, u.birthday as user_birthday,"
                + " f.login as friend_login, f.email as friend_email, f.name as friend_name, f.birthday as friend_birthday"
                + " from user_friends uf"
                + " inner join users u on uf.user_id = u.id"
                + " inner join users f on uf.friend_id = f.id"
                + " where u.id = ?"
                + " and f.id = ?";
        return getRecord(query, userId, friendId);
    }

    public boolean deleteFriendship(Friendship friendship) {
        String query = "delete from user_friends where id = ?";
        return delete(query, friendship.getId());
    }
}
