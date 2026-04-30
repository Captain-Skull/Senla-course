package com.senla.pas.mapper;

import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.dto.response.PaymentResponse;
import com.senla.pas.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PaymentMapper {

    @Mapping(target = "adId", source = "ad.id")
    @Mapping(target = "user", source = "user")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
