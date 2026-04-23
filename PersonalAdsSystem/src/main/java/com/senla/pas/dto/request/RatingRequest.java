package com.senla.pas.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    @NotNull(message = "Рейтинг не может быть null")
    @Max(value = 5, message = "Рейтинг не может быть больше 5")
    @Min(value = 1, message = "Рейтинг не может быть меньше 1")
    private Short rating;
}
