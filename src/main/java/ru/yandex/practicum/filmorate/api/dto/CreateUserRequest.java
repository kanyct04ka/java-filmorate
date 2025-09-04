package ru.yandex.practicum.filmorate.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;


@Data
public class CreateUserRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String login;

    private String name;

    @Past
    private LocalDate birthday;
}
