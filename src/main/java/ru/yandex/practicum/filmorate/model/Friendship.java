package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Friendship {
    private final User user;
    private final User friend;
    private boolean isFriend;

    public boolean getIsFriend() {
        return isFriend;
    }
}
