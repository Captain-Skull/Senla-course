package com.senla.pas.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleHistoryRequest {

    @Min(value = 1, message = "Цена должна быть больше 0")
    private Integer price;
}
