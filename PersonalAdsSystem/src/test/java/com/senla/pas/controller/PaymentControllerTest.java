package com.senla.pas.controller;

import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.dto.response.PaymentResponse;
import com.senla.pas.enums.PromotionPlan;
import com.senla.pas.exception.GlobalExceptionHandler;
import com.senla.pas.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest extends AbstractControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private com.senla.pas.controller.PaymentController paymentController;

    @BeforeEach
    void init() {
        this.mockMvc = buildMockMvc(paymentController, new GlobalExceptionHandler());
    }

    @Test
    void getMyPayments_happy() throws Exception {
        PaymentResponse r = new PaymentResponse(); r.setId(1L);
        when(paymentService.getMyPayments()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/payments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getPaymentById_happy() throws Exception {
        PaymentResponse r = new PaymentResponse(); r.setId(2L);
        when(paymentService.getPaymentById(2L)).thenReturn(r);

        mockMvc.perform(get("/api/payments/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void createPayment_happy() throws Exception {
        PaymentRequest req = new PaymentRequest(5L, PromotionPlan.DAY);
        PaymentResponse resp = new PaymentResponse(); resp.setId(10L);
        when(paymentService.createPayment(any())).thenReturn(resp);

        String json = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }
}
