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
        String query = "select * from user_friends where user_id = ?";
        return getRecords(query, id);
    }

    public Optional<Friendship> getFriendship(int userId, int friendId) {
        String query = "select * from user_friends"
                + " where user_id = ?"
                + " and friend_id = ?";
        return getRecord(query, userId, friendId);
    }

    public boolean deleteFriendship(Friendship friendship) {
        String query = "delete from user_friends where id = ?";
        return delete(query, friendship.getId());
    }
}
