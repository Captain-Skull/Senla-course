package com.senla.pas.dto.request;

import com.senla.pas.enums.AdCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdRequest {

    @Size(max = 100, message = "Название объявления не может превышать 100 символов")
    private String title;

    @Size(max = 3000, message = "Описание объявления не может превышать 3000 символов")
    private String description;

    private AdCategory category;

    @Min(value = 0, message = "Цена объявления не может быть отрицательной")
    private Integer price;

    private Boolean isActive;
}
