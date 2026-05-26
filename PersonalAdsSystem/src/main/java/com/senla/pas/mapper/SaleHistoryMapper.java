package com.senla.pas.mapper;

import com.senla.pas.dto.request.SaleHistoryRequest;
import com.senla.pas.dto.response.SaleHistoryResponse;
import com.senla.pas.entity.SaleHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface SaleHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "adTitle", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "soldAt", expression = "java(java.time.LocalDateTime.now())")
    SaleHistory toEntity(SaleHistoryRequest saleHistoryRequest);

    @Mapping(target = "adId", source = "ad.id")
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "buyer", source = "buyer")
    SaleHistoryResponse toResponse(SaleHistory saleHistory);

    List<SaleHistoryResponse> toResponseList(List<SaleHistory> saleHistories);
}
