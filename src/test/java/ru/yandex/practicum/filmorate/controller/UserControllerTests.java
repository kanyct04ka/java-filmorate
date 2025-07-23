package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserControllerTests {
    private UserController userController;

    @BeforeEach
    void prepareUserController() {
        userController = new UserController();
    }

    @Test
    void createUser_Success() {
        User user = User.builder()
                .email("adress@domain.zone")
                .login("vas01")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();

        assertEquals(1, userController.createUser(user).getId());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void createUser_SuccessSomeUsers() {
        int x = 3;
        for (int i = 1; i <= x; i++) {
            User user = User.builder()
                    .email("adress" + i + "@domain.zone")
                    .login("vas" + i)
                    .name("vasya")
                    .birthday(LocalDate.of(1980 + i, 01, 01))
                    .build();
            userController.createUser(user);
        }
        assertEquals(x, userController.getAllUsers().size());
    }

    @Test
    void createUser_LoginWithSpace() {
        User user = User.builder()
                .email("adress@domain.zone")
                .login("vas 01")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();
        Exception e = assertThrows(ValidationException.class,
                () -> {userController.createUser(user);});

        assertEquals("Логин не должен содержать пробелы", e.getMessage());
        assertEquals(0, userController.getAllUsers().size());
    }

    @Test
    void updateUser_Success() {
        User userForUpload = User.builder()
                .email("adress@domain.zone")
                .login("vas01")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();
        userController.createUser(userForUpload);

        User userForUpdate = User.builder()
                .id(userForUpload.getId())
                .email("adress@domain.zone")
                .login("vas01_updated")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();
        userController.updateUser(userForUpdate.getId(), userForUpdate);

        List<User> list = new ArrayList<>(userController.getAllUsers());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(userForUpdate.getLogin(), list.get(0).getLogin());
    }

    @Test
    void updateUser_InvalidIdInPath() {
        User userForUpload = User.builder()
                .email("adress@domain.zone")
                .login("vas01")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();
        userController.createUser(userForUpload);

        User userForUpdate = User.builder()
                .id(userForUpload.getId() * -1)
                .email("adress@domain.zone")
                .login("vas01_updated")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {userController.updateUser(userForUpdate.getId(), userForUpdate);});
        assertEquals("Id должен быть положительным числом", e.getMessage());

        List<User> list = new ArrayList<>(userController.getAllUsers());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(userForUpload.getLogin(), list.get(0).getLogin());
    }

    @Test
    void updateUser_DifferentIdInPathAndBody() {
        User userForUpload = User.builder()
                .email("adress@domain.zone")
                .login("vas01")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();
        userController.createUser(userForUpload);

        User userForUpdate = User.builder()
                .id(userForUpload.getId())
                .email("adress@domain.zone")
                .login("vas01_updated")
                .name("vasya")
                .birthday(LocalDate.of(1985, 01, 01))
                .build();

        Exception e = assertThrows(ValidationException.class,
                () -> {userController.updateUser(userForUpdate.getId() + 8, userForUpdate);});
        assertEquals("Не совпадает id в теле сообщения", e.getMessage());
        userController.updateUser(userForUpdate.getId(), userForUpdate);

        List<User> list = new ArrayList<>(userController.getAllUsers());

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(userForUpload.getLogin(), list.get(0).getLogin());
    }
}
