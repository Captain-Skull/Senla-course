package com.senla.pas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleHistoryResponse {

    private Long id;
    private Long adId;
    private UserShortResponse seller;
    private UserShortResponse buyer;
    private Integer price;
    private LocalDateTime soldAt;
}
