package com.senla.pas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Необходимо указать имя пользователя или почту")
    private String usernameOrEmail;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
