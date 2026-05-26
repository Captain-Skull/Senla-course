package com.senla.pas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private Long id;
    private Long adId;
    private String adTitle;
    private UserShortResponse buyer;
    private UserShortResponse seller;
    private LocalDateTime createdAt;
}
