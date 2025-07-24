package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

        int id;
        @NotBlank  //электронная почта не может быть пустой
        @Email     //и должна содержать символ @
        String email;
        @NotBlank  //логин не может быть пустым
        String login;
        String name;
        @Past   //дата рождения не может быть в будущем
        LocalDate birthday;
}
