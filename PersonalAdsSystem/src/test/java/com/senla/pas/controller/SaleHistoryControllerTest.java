package com.senla.pas.controller;

import com.senla.pas.dto.request.SaleHistoryRequest;
import com.senla.pas.dto.response.SaleHistoryResponse;
import com.senla.pas.service.SaleHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleHistoryControllerTest {

    @Mock
    private SaleHistoryService saleHistoryService;
    @InjectMocks
    private SaleHistoryController saleHistoryController;

    @Test
    void getMySales_positive() {
        List<SaleHistoryResponse> responses = List.of(new SaleHistoryResponse());
        when(saleHistoryService.getMySales()).thenReturn(responses);

        ResponseEntity<List<SaleHistoryResponse>> result = saleHistoryController.getMySales();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(responses, result.getBody());
        verify(saleHistoryService).getMySales();
    }

    @Test
    void getMySales_negative_serviceThrows() {
        when(saleHistoryService.getMySales()).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> saleHistoryController.getMySales());
    }

    @Test
    void getMySales_npeSafety_nullBodyAllowed() {
        when(saleHistoryService.getMySales()).thenReturn(null);

        assertDoesNotThrow(() -> saleHistoryController.getMySales());
    }

    @Test
    void getMyPurchases_positive() {
        List<SaleHistoryResponse> responses = List.of(new SaleHistoryResponse());
        when(saleHistoryService.getMyPurchases()).thenReturn(responses);

        ResponseEntity<List<SaleHistoryResponse>> result = saleHistoryController.getMyPurchases();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(responses, result.getBody());
        verify(saleHistoryService).getMyPurchases();
    }

    @Test
    void getMyPurchases_negative_serviceThrows() {
        when(saleHistoryService.getMyPurchases()).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> saleHistoryController.getMyPurchases());
    }

    @Test
    void getMyPurchases_npeSafety_nullBodyAllowed() {
        when(saleHistoryService.getMyPurchases()).thenReturn(null);

        assertDoesNotThrow(() -> saleHistoryController.getMyPurchases());
    }

    @Test
    void getSaleById_positive() {
        SaleHistoryResponse response = new SaleHistoryResponse();
        when(saleHistoryService.getSaleById(1L)).thenReturn(response);

        ResponseEntity<SaleHistoryResponse> result = saleHistoryController.getSaleById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(saleHistoryService).getSaleById(1L);
    }

    @Test
    void getSaleById_negative_serviceThrows() {
        when(saleHistoryService.getSaleById(1L)).thenThrow(new IllegalArgumentException("missing"));

        assertThrows(IllegalArgumentException.class, () -> saleHistoryController.getSaleById(1L));
    }

    @Test
    void getSaleById_npeSafety_nullSaleId() {
        when(saleHistoryService.getSaleById(null)).thenReturn(new SaleHistoryResponse());

        assertDoesNotThrow(() -> saleHistoryController.getSaleById(null));
    }

    @Test
    void buyDirectly_positive() {
        SaleHistoryResponse response = new SaleHistoryResponse();
        when(saleHistoryService.buyDirectly(2L)).thenReturn(response);

        ResponseEntity<SaleHistoryResponse> result = saleHistoryController.buyDirectly(2L);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(saleHistoryService).buyDirectly(2L);
    }

    @Test
    void buyDirectly_negative_serviceThrows() {
        when(saleHistoryService.buyDirectly(2L)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> saleHistoryController.buyDirectly(2L));
    }

    @Test
    void buyDirectly_npeSafety_nullAdId() {
        when(saleHistoryService.buyDirectly(null)).thenReturn(new SaleHistoryResponse());

        assertDoesNotThrow(() -> saleHistoryController.buyDirectly(null));
    }

    @Test
    void buyViaChat_positive() {
        SaleHistoryRequest request = new SaleHistoryRequest();
        SaleHistoryResponse response = new SaleHistoryResponse();
        when(saleHistoryService.buyViaChat(3L, request)).thenReturn(response);

        ResponseEntity<SaleHistoryResponse> result = saleHistoryController.buyViaChat(3L, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(saleHistoryService).buyViaChat(3L, request);
    }

    @Test
    void buyViaChat_negative_serviceThrows() {
        SaleHistoryRequest request = new SaleHistoryRequest();
        when(saleHistoryService.buyViaChat(3L, request)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> saleHistoryController.buyViaChat(3L, request));
    }

    @Test
    void buyViaChat_npeSafety_nullFields() {
        SaleHistoryRequest request = new SaleHistoryRequest();
        when(saleHistoryService.buyViaChat(3L, request)).thenReturn(new SaleHistoryResponse());

        assertDoesNotThrow(() -> saleHistoryController.buyViaChat(3L, request));
    }
}
