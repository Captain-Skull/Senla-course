package com.senla.pas.dto.response;

import com.senla.pas.enums.AdCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdResponse {

    private Long id;
    private String title;
    private String description;
    private AdCategory category;
    private UserShortResponse user;
    private int price;
    private Boolean isActive;
    private Boolean isPremium;
    private LocalDateTime createdAt;
}
