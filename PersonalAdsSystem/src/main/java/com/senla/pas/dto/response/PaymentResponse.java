package com.senla.pas.dto.response;

import com.senla.pas.enums.PromotionPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long adId;
    private UserShortResponse user;
    private PromotionPlan plan;
    private Integer amount;
    private LocalDateTime confirmedAt;
    private LocalDateTime expireAt;
}
