package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;


@Getter
@Setter
@RequiredArgsConstructor
public class UsersRelation {
    private int id;
    private final User initiator;
    private final User respondent;
    private UsersRelationStatus status;
}
