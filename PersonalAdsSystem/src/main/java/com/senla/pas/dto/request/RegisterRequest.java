package com.senla.pas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Необходимо имя пользователя")
    @Size(min = 3, max = 100, message = "Имя пользователя должно быть от 3 до 100 символов")
    private String username;

    @NotBlank(message = "Почта необходима")
    @Email(message = "Введите настоящую почту")
    @Size(max = 255, message = "Почта не должна превышать 255 символов")
    private String email;

    @NotBlank(message = "Необходим пароль")
    @Size(min = 6, max = 255, message = "Пароль должен быть от 6 до 255 символов")
    private String password;
}
