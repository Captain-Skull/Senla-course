package com.senla.pas.mapper;

import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.dto.response.PaymentResponse;
import com.senla.pas.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "confirmedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "expireAt", ignore = true)
    Payment toEntity(PaymentRequest paymentRequest);

    @Mapping(target = "adId", source = "ad.id")
    @Mapping(target = "user", source = "user")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
