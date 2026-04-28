package com.senla.pas.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 255, message = "Имя пользователя не может превышать 255 символов")
    private String newUsername;

    @Size(max = 255, message = "Почта не может превышать 255 символов")
    private String newEmail;

    @Size(max = 255, message = "Пароль не может превышать 255 символов")
    private String newPassword;

    @Size(max = 3000, message = "Описание не может превышать 3000 символов")
    private String newAboutMe;
}
