package com.senla.pas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private UserShortResponse reviewer;
    private UserShortResponse recipient;
    private Short rating;
    private LocalDateTime createdAt;
}
